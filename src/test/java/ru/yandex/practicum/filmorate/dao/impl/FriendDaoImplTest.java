package ru.yandex.practicum.filmorate.dao.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class FriendDaoImplTest {
    @Autowired
    private FriendDaoImpl friendDao;

    @Autowired
    private UserDbStorage userStorage;

    private Long userId1;
    private Long userId2;
    private Long userId3;

    @BeforeEach
    void setUp() {
        User user1 = new User();
        user1.setEmail("user1@example.com");
        user1.setLogin("user1");
        user1.setBirthday(LocalDate.of(1990, 1, 1));
        User created1 = userStorage.create(user1);
        userId1 = created1.getId();

        User user2 = new User();
        user2.setEmail("user2@example.com");
        user2.setLogin("user2");
        user2.setBirthday(LocalDate.of(1991, 1, 1));
        User created2 = userStorage.create(user2);
        userId2 = created2.getId();

        User user3 = new User();
        user3.setEmail("user3@example.com");
        user3.setLogin("user3");
        user3.setBirthday(LocalDate.of(1992, 1, 1));
        User created3 = userStorage.create(user3);
        userId3 = created3.getId();
    }

    @Test
    void testAddFriend() {
        friendDao.addFriend(userId1, userId2);
        List<Long> friends = friendDao.getFriends(userId1);

        assertEquals(1, friends.size());
        assertEquals(userId2, friends.get(0));
    }

    @Test
    void testRemoveFriend() {
        friendDao.addFriend(userId1, userId2);
        friendDao.removeFriend(userId1, userId2);
        List<Long> friends = friendDao.getFriends(userId1);

        assertTrue(friends.isEmpty());
    }

    @Test
    void testGetCommonFriends() {
        friendDao.addFriend(userId1, userId3);
        friendDao.addFriend(userId2, userId3);

        List<Long> commonFriends = friendDao.getCommonFriends(userId1, userId2);

        assertEquals(1, commonFriends.size());
        assertEquals(userId3, commonFriends.get(0));
    }

    @Test
    void testGetFriends() {
        friendDao.addFriend(userId1, userId2);
        friendDao.addFriend(userId1, userId3);
        List<Long> friends = friendDao.getFriends(userId1);

        assertEquals(2, friends.size());
        assertTrue(friends.contains(userId2));
        assertTrue(friends.contains(userId3));
    }
}