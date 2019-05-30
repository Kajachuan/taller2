from datetime import datetime
from taller2.app.app import db
from taller2.app.models.channel import Channel
from taller2.app.models.message import Message

class TestChannel(object):
    def setup_class(cls):
        cls.channel = Channel(channel_name = 'MyChannel', private = False, owner = 'Creator', creation_date = datetime.now())
        cls.channel.save()

    def test_information(self):
        assert self.channel.channel_name == 'MyChannel'
        assert self.channel.private == False
        assert self.channel.owner == 'Creator'
        assert self.channel.description == 'No description'
        assert self.channel.welcome_message == 'Welcome'

    def test_is_member(self):
        assert self.channel.is_member('Creator')

    def test_num_of_members(self):
        assert self.channel.num_of_members() == 1

    def test_is_public(self):
        assert self.channel.is_public()

    def test_add_member(self):
        Channel.add_member('MyChannel','Creator', 'IronMan')
        self.channel.reload()
        assert self.channel.is_member('IronMan') is True
        assert self.channel.num_of_members() == 2

    def test_delete_member_if_owner(self):
        Channel.delete_member('MyChannel', 'Creator', 'IronMan')
        self.channel.reload()
        assert self.channel.is_member('IronMan') is False
        assert self.channel.num_of_members() == 1

    def test_add_message(self):
        Channel.add_message('MyChannel', 'Creator', 'First message')
        self.channel.reload()
        message_list = self.channel.get_messages()
        assert 'First message' in [message.message for message in message_list]

    def test_get_n_messages(self):
        #1 is the last added message
        Channel.add_message('MyChannel', 'Creator', 'Second message')
        Channel.add_message('MyChannel', 'Creator', 'Third message')
        Channel.add_message('MyChannel', 'Creator', 'Fourth message')
        Channel.add_message('MyChannel', 'Creator', 'Fifth message')
        self.channel.reload()
        message_list = self.channel.get_messages(1, 4)
        message_list = [message.message for message in message_list]
        assert 'Fifth message' in message_list
        assert 'Second message' in message_list
        assert not 'First message' in message_list
        message_list = self.channel.get_messages(3, 5)
        message_list = [message.message for message in message_list]
        assert not 'Fifth message' in message_list
        assert not 'Fourth message' in message_list
        assert 'First message' in message_list

    def test_add_message_only_member(self):
        Channel.add_message('MyChannel', 'Hacker', 'Hacking message')
        self.channel.reload()
        message_list = self.channel.get_messages(1, 5)
        assert not 'Hacking message' in [message.message for message in message_list]
