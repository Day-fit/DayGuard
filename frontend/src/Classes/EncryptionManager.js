import sodium from 'libsodium-wrappers';

class EncryptionManager {
    constructor() {
        this.isInitialized = false;
        this.identityKeyPair = null;
        this.signedPreKeyPair = null;
        this.signedPreKeySignature = null;
        this.oneTimeKeys = [];
        this.sessionKeys = new Map(); // Map<userId, sessionKey>
        this.ephemeralKeys = new Map(); // Map<userId, ephemeralKeyPair>
        this.peerEphemeral = new Map(); // Map<userId, last seen peer ephemeral pub (base64 ORIGINAL)>
    }

    async initialize() {
        if (this.isInitialized) return;
        
        await sodium.ready;
        this.loadKeysFromStorage();
        this.isInitialized = true;
    }

    /**
     * Generate all required keys for registration
     * @returns {Object} Object containing all keys in base64 format
     */
    generateKeysForRegistration() {
        if (!this.isInitialized) {
            throw new Error('EncryptionManager not initialized');
        }

        // Generate Identity Key (Ed25519)
        this.identityKeyPair = sodium.crypto_sign_keypair();
        
        // Generate Signed Pre-Key (Curve25519 for ECDH)
        this.signedPreKeyPair = sodium.crypto_box_keypair();
        
        // Sign the SPK public key (curve25519) with IK private key (ed25519)
        this.signedPreKeySignature = sodium.crypto_sign_detached(
            this.signedPreKeyPair.publicKey,
            this.identityKeyPair.privateKey
        );

        // Generate One-Time Keys (Curve25519 for ECDH)
        const opkCount = 10; // Generate 10 OPKs
        this.oneTimeKeys = [];
        for (let i = 0; i < opkCount; i++) {
            const opkPair = sodium.crypto_box_keypair();
            this.oneTimeKeys.push(opkPair);
        }

        const exported = {
            ikPub: sodium.to_base64(this.identityKeyPair.publicKey, sodium.base64_variants.ORIGINAL),
            spkPub: sodium.to_base64(this.signedPreKeyPair.publicKey, sodium.base64_variants.ORIGINAL),
            spkSignature: sodium.to_base64(this.signedPreKeySignature, sodium.base64_variants.ORIGINAL),
            opkPubs: this.oneTimeKeys.map(opk => sodium.to_base64(opk.publicKey, sodium.base64_variants.ORIGINAL))
        };

        // Persist private keys locally for session establishment
        this.saveKeysToStorage();
        return exported;
    }

    /**
     * Upload SPK to server
     * @param {string} spkPublicKey - Base64 encoded SPK public key
     * @param {string} spkSignature - Base64 encoded SPK signature
     */
    async uploadSpk(spkPublicKey, spkSignature) {
        try {
            const response = await fetch('/api/v1/encryption/upload-spk', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                credentials: 'include',
                body: JSON.stringify({
                    spkPublicKey,
                    spkSignature
                })
            });

            if (!response.ok) {
                throw new Error(`SPK upload failed: ${response.status}`);
            }

            return await response.json();
        } catch (error) {
            console.error('Error uploading SPK:', error);
            throw error;
        }
    }

    /**
     * Upload OPK keys to server
     * @param {string[]} opkKeys - Array of base64 encoded OPK public keys
     */
    async uploadOpkKeys(opkKeys) {
        try {
            const response = await fetch('/api/v1/encryption/upload-opk-keys', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                credentials: 'include',
                body: JSON.stringify({
                    opkKeys
                })
            });

            if (!response.ok) {
                throw new Error(`OPK upload failed: ${response.status}`);
            }

            return await response.json();
        } catch (error) {
            console.error('Error uploading OPK keys:', error);
            throw error;
        }
    }

    saveKeysToStorage() {
        try {
            const data = {
                ikSk: sodium.to_base64(this.identityKeyPair?.privateKey || new Uint8Array(0), sodium.base64_variants.ORIGINAL),
                ikPk: sodium.to_base64(this.identityKeyPair?.publicKey || new Uint8Array(0), sodium.base64_variants.ORIGINAL),
                spkSk: sodium.to_base64(this.signedPreKeyPair?.privateKey || new Uint8Array(0), sodium.base64_variants.ORIGINAL),
                spkPk: sodium.to_base64(this.signedPreKeyPair?.publicKey || new Uint8Array(0), sodium.base64_variants.ORIGINAL),
                spkSig: this.signedPreKeySignature ? sodium.to_base64(this.signedPreKeySignature, sodium.base64_variants.ORIGINAL) : null
            };
            localStorage.setItem('dg_e2ee_keys', JSON.stringify(data));
        } catch (e) {
            console.warn('Failed to persist keys locally:', e);
        }
    }

    loadKeysFromStorage() {
        try {
            const raw = localStorage.getItem('dg_e2ee_keys');
            if (!raw) return;
            const data = JSON.parse(raw);
            if (data?.ikSk && data?.ikPk) {
                this.identityKeyPair = {
                    privateKey: sodium.from_base64(data.ikSk, sodium.base64_variants.ORIGINAL),
                    publicKey: sodium.from_base64(data.ikPk, sodium.base64_variants.ORIGINAL)
                };
            }
            if (data?.spkSk && data?.spkPk) {
                this.signedPreKeyPair = {
                    privateKey: sodium.from_base64(data.spkSk, sodium.base64_variants.ORIGINAL),
                    publicKey: sodium.from_base64(data.spkPk, sodium.base64_variants.ORIGINAL)
                };
            }
            if (data?.spkSig) {
                this.signedPreKeySignature = sodium.from_base64(data.spkSig, sodium.base64_variants.ORIGINAL);
            }
        } catch (e) {
            console.warn('Failed to load stored keys:', e);
        }
    }

    /**
     * Get pre-key bundle for a user
     * @param {string} userId - User UUID
     * @returns {Object} Pre-key bundle
     */
    async getPreKeyBundle(userId) {
        try {
            const response = await fetch(`/api/v1/encryption/user/${userId}/get-pre-key-bundle`, {
                method: 'GET',
                credentials: 'include'
            });

            if (!response.ok) {
                throw new Error(`Failed to get pre-key bundle: ${response.status}`);
            }

            return await response.json();
        } catch (error) {
            console.error('Error getting pre-key bundle:', error);
            throw error;
        }
    }

    /**
     * Verify SPK signature
     * @param {string} ikPub - Base64 encoded IK public key
     * @param {string} spkPub - Base64 encoded SPK public key
     * @param {string} spkSignature - Base64 encoded SPK signature
     * @returns {boolean} True if signature is valid
     */
    verifySpkSignature(ikPub, spkPub, spkSignature) {
        try {
            const ikPubBytes = sodium.from_base64(ikPub, sodium.base64_variants.ORIGINAL);
            const spkPubBytes = sodium.from_base64(spkPub, sodium.base64_variants.ORIGINAL);
            const spkSigBytes = sodium.from_base64(spkSignature, sodium.base64_variants.ORIGINAL);

            return sodium.crypto_sign_verify_detached(spkSigBytes, spkPubBytes, ikPubBytes);
        } catch (error) {
            console.error('Error verifying SPK signature:', error);
            return false;
        }
    }

    /**
     * Perform X3DH key agreement
     * @param {string} userId - Target user ID
     * @param {Object} preKeyBundle - Pre-key bundle from server
     * @returns {Object} Session key and ephemeral key pair
     */
    async performX3DHKeyAgreement(userId, preKeyBundle) {
        if (!this.isInitialized) {
            throw new Error('EncryptionManager not initialized');
        }

        if (!preKeyBundle || !preKeyBundle.ikPub || !preKeyBundle.spkPub || !preKeyBundle.spkSignature) {
            throw new Error('Pre-key bundle is incomplete');
        }

        if (!this.identityKeyPair || !this.identityKeyPair.privateKey) {
            throw new Error('Identity key is unavailable. Please re-login or re-register to restore keys.');
        }

        // Verify SPK signature
        if (!this.verifySpkSignature(preKeyBundle.ikPub, preKeyBundle.spkPub, preKeyBundle.spkSignature)) {
            throw new Error('Invalid SPK signature');
        }

        // Generate ephemeral key pair (Curve25519)
        const ephemeralKeyPair = sodium.crypto_box_keypair();
        this.ephemeralKeys.set(userId, ephemeralKeyPair);

        // Convert base64 keys to bytes
        const ikPubBytes = sodium.from_base64(preKeyBundle.ikPub, sodium.base64_variants.ORIGINAL);
        const spkPubBytes = sodium.from_base64(preKeyBundle.spkPub, sodium.base64_variants.ORIGINAL);

        // X3DH aligned derivation using only DH2 and DH3
        // DH2: EK_A * IK_B(curve)
        const ikCurvePkB = sodium.crypto_sign_ed25519_pk_to_curve25519(ikPubBytes);
        const dh2 = sodium.crypto_scalarmult(ephemeralKeyPair.privateKey, ikCurvePkB);
        // DH3: EK_A * SPK_B
        const dh3 = sodium.crypto_scalarmult(ephemeralKeyPair.privateKey, spkPubBytes);

        // Combine DH outputs
        const parts = [dh2, dh3];
        const totalLen = parts.reduce((acc, p) => acc + p.length, 0);
        const combined = new Uint8Array(totalLen);
        let offset = 0;
        for (const p of parts) {
            combined.set(p, offset);
            offset += p.length;
        }

        // Derive session key using HKDF
        const sessionKey = sodium.crypto_generichash(32, combined);
        this.sessionKeys.set(userId, sessionKey);

        return {
            sessionKey,
            ephemeralKeyPair,
            ephemeralPub: sodium.to_base64(ephemeralKeyPair.publicKey, sodium.base64_variants.ORIGINAL)
        };
    }

    /**
     * Encrypt message for a user
     * @param {string} userId - Target user ID
     * @param {string} message - Plain text message
     * @returns {Object} Encrypted message with ephemeral public key
     */
    async encryptMessage(userId, message) {
        if (!this.isInitialized) {
            throw new Error('EncryptionManager not initialized');
        }

        let sessionKey = this.sessionKeys.get(userId);
        let ephemeralPub = null;

        // If no session key exists, perform X3DH key agreement
        if (!sessionKey) {
            const preKeyBundle = await this.getPreKeyBundle(userId);
            const keyAgreement = await this.performX3DHKeyAgreement(userId, preKeyBundle);
            sessionKey = keyAgreement.sessionKey;
            ephemeralPub = keyAgreement.ephemeralPub;
        } else {
            // Use existing ephemeral key
            const ephemeralKeyPair = this.ephemeralKeys.get(userId);
            if (ephemeralKeyPair) {
                ephemeralPub = sodium.to_base64(ephemeralKeyPair.publicKey, sodium.base64_variants.ORIGINAL);
            }
        }

        // Generate random nonce
        const nonce = sodium.randombytes_buf(sodium.crypto_secretbox_NONCEBYTES);
        
        // Encrypt message
        const messageBytes = sodium.from_string(message);
        const ciphertext = sodium.crypto_secretbox_easy(messageBytes, nonce, sessionKey);
        
        // Combine nonce and ciphertext
        const encryptedData = new Uint8Array(nonce.length + ciphertext.length);
        encryptedData.set(nonce, 0);
        encryptedData.set(ciphertext, nonce.length);

        return {
            ciphertext: sodium.to_base64(encryptedData, sodium.base64_variants.ORIGINAL),
            ephemeralPub
        };
    }

    /**
     * Decrypt message from a user
     * @param {string} userId - Sender user ID
     * @param {string} ciphertext - Base64 encoded encrypted message
     * @param {string} ephemeralPub - Base64 encoded ephemeral public key
     * @returns {string} Decrypted message
     */
    async decryptMessage(userId, ciphertext, ephemeralPub) {
        if (!this.isInitialized) {
            throw new Error('EncryptionManager not initialized');
        }

        // Ensure session exists and matches current peer ephemeral
        let sessionKey = this.sessionKeys.get(userId);
        const lastPeerEphemeral = this.peerEphemeral.get(userId);
        const shouldRekey = !sessionKey || (ephemeralPub && lastPeerEphemeral && lastPeerEphemeral !== ephemeralPub);
        if (shouldRekey && ephemeralPub) {
            sessionKey = await this.deriveSessionKeyFromEphemeral(userId, ephemeralPub);
            this.peerEphemeral.set(userId, ephemeralPub);
        } else if (!sessionKey && !ephemeralPub) {
            throw new Error('No session key available for decryption');
        }

        if (!sessionKey) {
            throw new Error('No session key available for decryption');
        }

        // Decode encrypted data
        const encryptedData = sodium.from_base64(ciphertext, sodium.base64_variants.ORIGINAL);
        const nonce = encryptedData.slice(0, sodium.crypto_secretbox_NONCEBYTES);
        const encryptedMessage = encryptedData.slice(sodium.crypto_secretbox_NONCEBYTES);

        // Decrypt message with one-shot re-derive fallback
        try {
            const decryptedBytes = sodium.crypto_secretbox_open_easy(encryptedMessage, nonce, sessionKey);
            return sodium.to_string(decryptedBytes);
        } catch (e) {
            if (ephemeralPub) {
                // Force re-derive and retry once
                const rederived = await this.deriveSessionKeyFromEphemeral(userId, ephemeralPub);
                const retried = sodium.crypto_secretbox_open_easy(encryptedMessage, nonce, rederived);
                return sodium.to_string(retried);
            }
            throw e;
        }
    }

    /**
     * Derive session key from ephemeral public key (receiver side)
     * @param {string} userId - Sender user ID
     * @param {string} ephemeralPub - Base64 encoded ephemeral public key
     * @returns {Uint8Array} Session key
     */
    async deriveSessionKeyFromEphemeral(userId, ephemeralPub) {
        if (!this.isInitialized) {
            throw new Error('EncryptionManager not initialized');
        }

        // Convert ephemeral public key to bytes
        const ephemeralPubBytes = sodium.from_base64(ephemeralPub, sodium.base64_variants.ORIGINAL);

        // Receiver-side aligned derivation with DH2 and DH3
        // DH2: IK_B(curve) * EK_A
        const ikCurveSkB = sodium.crypto_sign_ed25519_sk_to_curve25519(this.identityKeyPair.privateKey);
        const dh2 = sodium.crypto_scalarmult(ikCurveSkB, ephemeralPubBytes);
        // DH3: SPK_B * EK_A
        const dh3 = sodium.crypto_scalarmult(this.signedPreKeyPair.privateKey, ephemeralPubBytes);

        // Combine and derive session key
        const combined = new Uint8Array(dh2.length + dh3.length);
        combined.set(dh2, 0);
        combined.set(dh3, dh2.length);
        const sessionKey = sodium.crypto_generichash(32, combined);
        this.sessionKeys.set(userId, sessionKey);

        return sessionKey;
    }

    /**
     * Clear session data for a user
     * @param {string} userId - User ID
     */
    clearSession(userId) {
        this.sessionKeys.delete(userId);
        this.ephemeralKeys.delete(userId);
    }

    /**
     * Clear all session data
     */
    clearAllSessions() {
        this.sessionKeys.clear();
        this.ephemeralKeys.clear();
    }

    /**
     * Get current session status
     * @returns {Object} Session status information
     */
    getSessionStatus() {
        return {
            isInitialized: this.isInitialized,
            hasIdentityKey: !!this.identityKeyPair,
            hasSignedPreKey: !!this.signedPreKeyPair,
            oneTimeKeyCount: this.oneTimeKeys.length,
            activeSessions: this.sessionKeys.size
        };
    }
}

export default EncryptionManager;
