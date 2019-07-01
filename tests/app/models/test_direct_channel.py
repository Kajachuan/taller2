from taller2.app.models.direct_channel import DirectChannel
from taller2.app.models.user import User
from taller2.app.models.organization import Organization
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
        cls.organization = Organization(owner = cls.user1, organization_name = 'My Organization Direct', creation_date = datetime.now())
        cls.organization.save()

    def test_only_has_two_members(self):
        dchannel = DirectChannel(members = [self.user1, self.user2, self.user3])
        with pytest.raises(InvalidAmountOfMembers):
            assert dchannel.save()

    def test_channel_name_is_some_user(self):
        dchannel = DirectChannel(members = [self.user1, self.user2])
        dchannel.save()
        assert dchannel.channel_name == self.user1.username or dchannel.channel_name == self.user2.username

    def test_get_messages_empty(self):
        dchannel = DirectChannel(members = [self.user1, self.user2])
        dchannel.save()
        messages = dchannel.get_messages(since = 1, to = 5)
        assert messages == []

    def test_add_message(self):
        dchannel = DirectChannel(members = [self.user1, self.user2])
        dchannel.save()
        self.organization.update(push__direct_channels = dchannel)
        DirectChannel.add_message('My Organization Direct', 'elnombre', 'elnombre2', 'message1')
        dchannel.reload()
        messages = dchannel.get_messages(since = 1, to = 5)
        assert messages[0].message == 'message1'
