import uuid
from base64 import b64encode
from os import environ, path
from datetime import datetime
from ..app import db
from .user import User
from .channel import Channel

class Organization(db.Document):
    organization_name = db.StringField(required = True, unique = True)
    owner = db.ReferenceField(User, required = True)
    moderators = db.ListField(db.ReferenceField(User))
    members = db.ListField(db.ReferenceField(User))
    ubication = db.StringField(default = 'Not Specified')
    encoded_image = db.StringField(required=False,
                                   default=b64encode(open(path
                                                     .abspath(path
                                                     .join(path
                                                     .dirname(__file__),
                                                     '../static/img/default_image.png')),
                                                     'rb')
                                                     .read())
                                                     .decode())
    description = db.StringField(default = 'Organization Information')
    welcome_message = db.StringField(default = 'Welcome')
    pending_invitations = db.MapField(db.StringField())
    channels = db.ListField(db.ReferenceField('Channel'))
    creation_date = db.DateTimeField(required=True)
    ban_date = db.DateTimeField(required=False)
    ban_reason = db.StringField(required=False)
    #map_of_active_users ?
    meta = {'strict': False}

    def is_member(self, user):
        return user in self.members

    def is_moderator(self, user):
        return user in self.moderators

    def is_owner(self, user):
        return user == self.owner

    def invite_user(self, user):
        return str(uuid.uuid1())

    def is_valid_token(self, token):
        return token in self.pending_invitations.keys()

    @classmethod
    def has_member(cls, organization_name, username):
        organization = cls.objects.get(organization_name = organization_name)
        user = User.objects.get(username = username)
        return organization.is_member(user)

    @classmethod
    def add_user(cls, username, organization_name, token):
        organization = Organization.objects.get(organization_name = organization_name)
        user = User.objects.get(username = username)
        organization.update(push__members = user)
        organization.update(**{'unset__pending_invitations__' + token : user.username})
        for channel in organization.channels:
            if channel.is_public():
                channel.update(push__members = username)

    @classmethod
    def create_channel(cls,organization_name, channel_name, owner, private):
        organization = cls.objects.get(organization_name = organization_name)
        if channel_name in [channel.channel_name for channel in organization.channels]:
            return False
        channel = Channel(channel_name, owner, private, datetime.now())
        channel.save()
        organization.update(push__channels = channel)
        return True

    @classmethod
    def get_channel(cls, organization_name, channel_name):
        organization = cls.objects.get(organization_name = organization_name)
        for channel in organization.channels:
            if channel.channel_name == channel_name:
                return channel

    @classmethod
    def get_channel_members(cls, organization_name, channel_name):
        organization = Organization.objects.get(organization_name = organization_name)
        members = []
        for channel in organization.channels:
            if channel.channel_name == channel_name:
                members = channel.members
        return members

    @classmethod
    def add_member_to_channel(cls, organization_name, channel_name, member_name):
        organization = Organization.objects.get(organization_name = organization_name)
        member = User.objects.get(username = member_name)
        channel = [channel for channel in organization.channels if channel.channel_name == channel_name].pop()
        channel.update(push__members = member_name)
