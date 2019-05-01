from ..app import db
from taller2.app.models.user import User
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

    def add_new_member(self, new_member):
        self.members.append(new_member)

    def is_member(self, user):
        return user in self.members

    def add_moderator(self, member):
        self.moderators.append(member)

    def is_moderator(self, user):
        return user in self.moderators

    def delete_member(self, member):
        if self.is_moderator(member):
            self.moderators.remove(member)
        self.members.remove(member)

    def remove_moderator(self, member):
        self.moderators.remove(member)

    def invite_user(self, user):
        return str(uuid.uuid1())

    def is_valid_token(self, token):
        print('is_valid')
        return token in self.pending_invitations.keys()
