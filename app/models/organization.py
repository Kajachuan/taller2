from ..app import db
from .user import User
import uuid

class Organization(db.Document):
    organization_name = db.StringField(required = True, unique = True)
    owner = db.ReferenceField(User, required = True)
    moderators = db.ListField(db.ReferenceField(User))
    members = db.ListField(db.ReferenceField(User))
    ubication = db.StringField(default = 'Not Specified')
    image_link = db.URLField()
    description = db.StringField(default = 'Organization Information')
    welcome_message = db.StringField(default = 'Welcome')
    pending_invitations = db.MapField(db.StringField())
    #channels = db.ListField(db.ReferenceField(Channel))
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
