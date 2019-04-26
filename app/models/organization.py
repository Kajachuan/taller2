from ..app import db
from taller2.app.models.user import User

class Organization(db.Document):
    organization_name = db.StringField(required = True, unique = True)
    owner = db.ReferenceField(User, required = True)
    moderators = db.ListField(db.ReferenceField(User))
    members = db.ListField(db.ReferenceField(User))
    #meta = {'strict': False) ?

    def add_new_member(self, new_member):
        self.members.append(new_member)

    def is_member(self, user):
        return user in self.members

    def add_moderator(self, member):
        self.moderators.append(member)

    def is_moderator(self, user):
        return user in self.moderators
