from taller2.app.models.direct_channel import DirectChannel
from taller2.app.models.user import User
from taller2.app.exceptions.channel_error import InvalidAmountOfMembers
from datetime import datetime
import pytest

class TestDirectChannel(object):
    def setup_class(cls):
        cls.user1 = User(username='elnombre', email='user@test.com', crypted_password='mipass', creation_date = datetime.now())
        cls.user2 = User(username='elnombre2', email='user@test.com', crypted_password='mipass', creation_date = datetime.now())
        cls.user3 = User(username='elnombre3', email='user@test.com', crypted_password='mipass', creation_date = datetime.now())
        cls.user1.save()
        cls.user2.save()
        cls.user3.save()

    def test_only_has_two_members(self):
        dchannel = DirectChannel(members = [self.user1, self.user2, self.user3])
        with pytest.raises(InvalidAmountOfMembers):
            assert dchannel.save()

    def test_channel_name_is_some_user(self):
        dchannel = DirectChannel(members = [self.user1, self.user2])
        dchannel.save()
        assert dchannel.channel_name == self.user1.username or dchannel.channel_name == self.user2.username
