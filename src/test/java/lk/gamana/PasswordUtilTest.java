package lk.gamana;

import lk.gamana.util.PasswordUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordUtilTest {

    @Test
    void testHashAndMatch() {
        String rawPassword = "SecretPassword123!";
        String hash = PasswordUtil.hash(rawPassword);

        assertNotNull(hash);
        assertTrue(hash.contains(":"), "Hash must contain salt and hash separated by colon");

        assertTrue(PasswordUtil.matches(rawPassword, hash), "Password should match generated hash");
        assertFalse(PasswordUtil.matches("WrongPassword", hash), "Wrong password must not match");
    }

    @Test
    void testDifferentSaltsProduceDifferentHashes() {
        String password = "IdenticalPassword!";
        String hash1 = PasswordUtil.hash(password);
        String hash2 = PasswordUtil.hash(password);

        assertNotEquals(hash1, hash2, "Two hashes of same password must differ due to random salt");
        assertTrue(PasswordUtil.matches(password, hash1));
        assertTrue(PasswordUtil.matches(password, hash2));
    }
}
