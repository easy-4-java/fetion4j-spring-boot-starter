package net.apexes.fetion4j.core.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the user-domain types in {@code net.apexes.fetion4j.core.user}.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
@DisplayName("User Domain Tests")
class UserDataTest {

    @Nested
    @DisplayName("User")
    class UserTest {
        @Test
        void testDefaultConstructor() {
            assertThat(new User()).isNotNull();
        }

        @Test
        void testUserIdConstructor() {
            assertThat(new User(5).getUserId()).isEqualTo(5);
        }

        @Test
        void testFullConstructorAndGetters() {
            User u = new User(7, "sip:7@fetion.com.cn;p=1", "alice");
            assertThat(u.getUserId()).isEqualTo(7);
            assertThat(u.getUri()).isEqualTo("sip:7@fetion.com.cn;p=1");
            assertThat(u.getName()).isEqualTo("alice");
        }

        @Test
        void testSetters() {
            User u = new User();
            u.setUserId(11);
            u.setUri("tel:13900000000");
            u.setName("bob");
            assertThat(u.getUserId()).isEqualTo(11);
            assertThat(u.getUri()).isEqualTo("tel:13900000000");
            assertThat(u.getName()).isEqualTo("bob");
        }

        @Test
        void testEqualsAndHashCodeByUserId() {
            User a = new User(1, "uri-a", "name-a");
            User b = new User(1, "uri-b", "name-b");
            User c = new User(2, "uri-a", "name-a");
            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
            assertThat(a).isNotEqualTo(c);
            assertThat(a).isNotEqualTo(null);
            assertThat(a).isNotEqualTo("string");
        }

        @Test
        void testToStringContainsFields() {
            User u = new User(3, "u", "n");
            assertThat(u.toString()).contains("userId=3").contains("uri=u").contains("name=n");
        }
    }

    @Nested
    @DisplayName("Personal")
    class PersonalTest {
        @Test
        void testDefaultConstructor() {
            assertThat(new Personal()).isNotNull();
        }

        @Test
        void testAllSettersAndGetters() {
            Personal p = new Personal();
            p.setVersion("v1");
            p.setSid("sid");
            p.setMobileNo(13900000000L);
            p.setNickname("nick");
            p.setImpresa("sig");
            p.setCarrier("CMCC");
            p.setCarrierStatus("0");
            p.setSmsOnlineStatus("0.0:0:0");
            p.setPresence(Presence.ONLINE);
            assertThat(p.getVersion()).isEqualTo("v1");
            assertThat(p.getSid()).isEqualTo("sid");
            assertThat(p.getMobileNo()).isEqualTo(13900000000L);
            assertThat(p.getNickname()).isEqualTo("nick");
            assertThat(p.getImpresa()).isEqualTo("sig");
            assertThat(p.getCarrier()).isEqualTo("CMCC");
            assertThat(p.getCarrierStatus()).isEqualTo("0");
            assertThat(p.getSmsOnlineStatus()).isEqualTo("0.0:0:0");
            assertThat(p.getPresence()).isEqualTo(Presence.ONLINE);
        }

        @Test
        void testSupportSmsTrue() {
            Personal p = new Personal();
            p.setCarrier("CMCC");
            p.setCarrierStatus("0");
            p.setSmsOnlineStatus("0.0:0:0");
            assertThat(p.supportSMS()).isTrue();
        }

        @Test
        void testSupportSmsFalseWhenCarrierNotCmcc() {
            Personal p = new Personal();
            p.setCarrier("");
            p.setCarrierStatus("0");
            p.setSmsOnlineStatus("0.0:0:0");
            assertThat(p.supportSMS()).isFalse();
        }

        @Test
        void testSupportSmsFalseWhenCarrierStatusNot0() {
            Personal p = new Personal();
            p.setCarrier("CMCC");
            p.setCarrierStatus("1");
            p.setSmsOnlineStatus("0.0:0:0");
            assertThat(p.supportSMS()).isFalse();
        }

        @Test
        void testSupportSmsFalseWhenSmsOnlineStatusNotZero() {
            Personal p = new Personal();
            p.setCarrier("CMCC");
            p.setCarrierStatus("0");
            p.setSmsOnlineStatus("365.0:0:0");
            assertThat(p.supportSMS()).isFalse();
        }

        @Test
        void testDisplayNamePrefersName() {
            Personal p = new Personal(1, "u", "alice");
            p.setNickname("nick");
            assertThat(p.getDisplayName()).isEqualTo("alice");
        }

        @Test
        void testDisplayNameFallsBackToNicknameWhenNameEmpty() {
            Personal p = new Personal(1, "u", "");
            p.setNickname("nick");
            assertThat(p.getDisplayName()).isEqualTo("nick");
        }

        @Test
        void testDisplayNameFallsBackToNicknameWhenNameNull() {
            Personal p = new Personal(1, "u", null);
            p.setNickname("nick");
            assertThat(p.getDisplayName()).isEqualTo("nick");
        }

        @Test
        void testDisplayNameFallsBackToUserId() {
            Personal p = new Personal(42, "u", null);
            assertThat(p.getDisplayName()).isEqualTo("42");
        }

        @Test
        void testToStringContainsKeyFields() {
            Personal p = new Personal(1, "u", "n");
            p.setVersion("v");
            assertThat(p.toString()).contains("version=v").contains("userId=1");
        }
    }

    @Nested
    @DisplayName("Buddy")
    class BuddyTest {
        @Test
        void testConstructorAndRelation() {
            Buddy b = new Buddy(1, "u", "n", Relation.BUDDY);
            assertThat(b.getRelation()).isEqualTo(Relation.BUDDY);
            b.setRelation(Relation.STRANGER);
            assertThat(b.getRelation()).isEqualTo(Relation.STRANGER);
        }

        @Test
        void testDefaultConstructor() {
            assertThat(new Buddy()).isNotNull();
        }

        @Test
        void testToStringRuns() {
            Buddy b = new Buddy(1, "u", "n", Relation.BUDDY);
            assertThat(b.toString()).contains("Buddy{");
        }
    }

    @Nested
    @DisplayName("Contact")
    class ContactTest {
        @Test
        void testVersion() {
            Contact c = new Contact("v1");
            assertThat(c.getVersion()).isEqualTo("v1");
            c.setVersion("v2");
            assertThat(c.getVersion()).isEqualTo("v2");
        }

        @Test
        void testAddAndFindBuddyByUserIdAndUri() {
            Contact c = new Contact("v1");
            Buddy b1 = new Buddy(1, "uri1", "n1", Relation.BUDDY);
            Buddy b2 = new Buddy(2, "uri2", "n2", Relation.BUDDY);
            c.addBuddy(b1);
            c.addBuddy(b2, "v2");
            assertThat(c.getVersion()).isEqualTo("v2");
            assertThat(c.findBuddy(1)).isEqualTo(b1);
            assertThat(c.findBuddy("uri2")).isEqualTo(b2);
            assertThat(c.findBuddy(99)).isNull();
            assertThat(c.findBuddy("missing")).isNull();
        }

        @Test
        void testRemoveBuddy() {
            Contact c = new Contact("v1");
            Buddy b1 = new Buddy(1, "uri1", "n1", Relation.BUDDY);
            c.addBuddy(b1);
            c.removeBuddy(b1, "v3");
            assertThat(c.findBuddy(1)).isNull();
            assertThat(c.getVersion()).isEqualTo("v3");
        }

        @Test
        void testBuddyGroupsAndBlacklist() {
            Contact c = new Contact("v1");
            BuddyGroup g = new BuddyGroup(10, "group");
            c.addBuddyGroup(g);
            assertThat(c.getBuddyGroups()).contains(g);
            Buddy b = new Buddy(5, "u", "n", Relation.BUDDY);
            c.addBuddy(b);
            User u = new User(1);
            c.addBlacklist(u);
            assertThat(c.getBlacklist()).contains(u);
            assertThat(c.getBuddys()).isNotEmpty();
        }

        @Test
        void testToStringIncludesVersionAndLists() {
            Contact c = new Contact("v9");
            c.addBuddy(new Buddy(1, "u", "n", Relation.BUDDY));
            c.addBlacklist(new User(2));
            assertThat(c.toString()).contains("v9").contains("[好友列表]").contains("[黑名单]");
        }
    }

    @Nested
    @DisplayName("BuddyGroup")
    class BuddyGroupTest {
        @Test
        void testGetters() {
            BuddyGroup g = new BuddyGroup(7, "friends");
            assertThat(g.getId()).isEqualTo(7);
            assertThat(g.getName()).isEqualTo("friends");
        }

        @Test
        void testToStringReturnsName() {
            assertThat(new BuddyGroup(1, "work").toString()).isEqualTo("work");
        }

        @Test
        void testEqualsAndHashCodeById() {
            BuddyGroup a = new BuddyGroup(1, "a");
            BuddyGroup b = new BuddyGroup(1, "b");
            BuddyGroup c = new BuddyGroup(2, "a");
            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
            assertThat(a).isNotEqualTo(c);
            assertThat(a).isNotEqualTo(null);
            assertThat(a).isNotEqualTo("x");
        }
    }

    @Nested
    @DisplayName("Presence enum")
    class PresenceTest {
        @Test
        void testValueOfKnownValues() {
            assertThat(Presence.valueOf(0)).isEqualTo(Presence.OFFLINE);
            assertThat(Presence.valueOf(100)).isEqualTo(Presence.AWAY);
            assertThat(Presence.valueOf(400)).isEqualTo(Presence.ONLINE);
            assertThat(Presence.valueOf(499)).isEqualTo(Presence.ROBOT);
            assertThat(Presence.valueOf(600)).isEqualTo(Presence.BUSY);
            assertThat(Presence.valueOf(123)).isEqualTo(Presence.UNKNOWN);
        }

        @Test
        void testGetValue() {
            assertThat(Presence.ONLINE.getValue()).isEqualTo(400);
            assertThat(Presence.OFFLINE.getValue()).isZero();
            assertThat(Presence.UNKNOWN.getValue()).isEqualTo(-1);
        }
    }

    @Nested
    @DisplayName("Relation enum")
    class RelationTest {
        @Test
        void testValueOfKnownValues() {
            assertThat(Relation.valueOf(0)).isEqualTo(Relation.UNCONFIRMED);
            assertThat(Relation.valueOf(1)).isEqualTo(Relation.BUDDY);
            assertThat(Relation.valueOf(2)).isEqualTo(Relation.DECLINED);
            assertThat(Relation.valueOf(3)).isEqualTo(Relation.STRANGER);
            assertThat(Relation.valueOf(4)).isEqualTo(Relation.BANNED);
        }

        @Test
        void testValueOfInvalidThrows() {
            assertThatThrownBy(() -> Relation.valueOf(99))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void testGetValue() {
            assertThat(Relation.BUDDY.getValue()).isEqualTo(1);
            assertThat(Relation.BANNED.getValue()).isEqualTo(4);
        }
    }
}
