import { validateEmail, validateUsername, validatePassword } from '../src/main.js';
import assert from 'assert';

describe('Validation Helpers', function() {
  describe('validateEmail', function() {
    it('should validate correct emails', function() {
      assert.strictEqual(validateEmail('test@example.com'), true);
      assert.strictEqual(validateEmail('user.name+tag@domain.co.uk'), true);
    });
    it('should invalidate incorrect emails', function() {
      assert.strictEqual(validateEmail('not-an-email'), false);
      assert.strictEqual(validateEmail('a@b'), false);
      assert.strictEqual(validateEmail(''), false);
    });
  });

  describe('validateUsername', function() {
    it('should validate correct usernames', function() {
      assert.strictEqual(validateUsername('user123'), true);
      assert.strictEqual(validateUsername('abcDEF123'), true);
    });
    it('should invalidate incorrect usernames', function() {
      assert.strictEqual(validateUsername('ab'), false); // too short
      assert.strictEqual(validateUsername('user!@#'), false); // special chars
      assert.strictEqual(validateUsername(''), false);
      assert.strictEqual(validateUsername('a'.repeat(21)), false); // too long
    });
  });

  describe('validatePassword', function() {
    it('should validate strong passwords', function() {
      assert.strictEqual(validatePassword('StrongPass123!@#'), true);
      assert.strictEqual(validatePassword('AnotherGood1!'), true);
    });
    it('should invalidate weak passwords', function() {
      assert.strictEqual(validatePassword('short'), false);
      assert.strictEqual(validatePassword('12345678'), false);
      assert.strictEqual(validatePassword('password'), false);
      assert.strictEqual(validatePassword('a'.repeat(65)), false); // too long
    });
  });
}); 