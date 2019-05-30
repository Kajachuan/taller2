from ..app import db
from .message import Message
import datetime as dt

class Channel(db.Document):
    channel_name = db.StringField(required = True)
    owner = db.StringField(required = True)
    private = db.BooleanField(required = True)
    description = db.StringField(default = 'No description', max_length = 100)
    welcome_message = db.StringField(default = 'Welcome', max_length = 30)

    members = db.ListField(db.StringField())
    messages = db.ListField(db.ReferenceField('Message'))
    meta = {'strict': False}

    def clean(self):
        self.members.append(self.owner)

    def is_member(self, username):
        return username in self.members

    def is_owner(self, owner):
        return self.owner == owner

    def is_public(self):
        return not self.private

    def num_of_members(self):
        return len(self.members)

    def get_messages(self, since = 1, to = 1):
        start = len(self.messages) - to
        end = len(self.messages) - since + 1
        return self.messages[start: end]

    @classmethod
    def add_member(cls, channel_name, requester, username):
        channel = cls.objects.get(channel_name = channel_name)
        if channel.is_member(requester):
            channel.update(push__members = username)

    @classmethod
    def delete_member(cls, channel_name, requester, username):
        channel = cls.objects.get(channel_name = channel_name)
        if channel.is_owner(requester):
            channel.update(pull__members = username)

    @classmethod
    def add_message(cls, channel_name, requester, message):
        channel = cls.objects.get(channel_name = channel_name)
        if channel.is_member(requester):
            message = Message(timestamp = dt.datetime.today(), sender = requester, message = message)
            message.save()
            channel.update(push__messages = message)
