from taller2.app.app import db
from taller2.app.models.organization import Organization
from taller2.app.models.user import User

class TestOrganization(object):
    def setup_method(self, method):
        self.owner = User(username='MiNombre', email='user@test.com', crypted_password='mipass')
        self.organization = Organization(owner = self.owner, organization_name = 'My organization')

    def test_new_organization(self):
        assert self.organization.owner == self.owner
        assert self.organization.organization_name == 'My organization'
        assert len(self.organization.moderators) == 0
        assert len(self.organization.members) == 0

    def test_add_new_member(self):
        member = User(username='Member', email='member@test.com', crypted_password='mipass')
        self.organization.add_new_member(member)
        assert len(self.organization.members) == 1
        assert self.organization.is_member(member) is True

    def test_set_member_as_moderator(self):
        member = User(username='Member', email='member@test.com', crypted_password='mipass')
        self.organization.add_new_member(member)
        self.organization.add_moderator(member)
        assert len(self.organization.members) == 1
        assert len(self.organization.moderators) == 1
        assert self.organization.is_moderator(member) is True
