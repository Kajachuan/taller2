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

    def test_default_info_organization(self):
        assert self.organization.ubication == 'Not Specified'
        assert self.organization.description == 'Organization Information'
        assert self.organization.welcome_message == 'Welcome'

    def test_add_new_member(self):
        member = User(username='Member', email='member@test.com', crypted_password='mipass')
        self.organization.members.append(member)
        assert len(self.organization.members) == 1
        assert self.organization.is_member(member) is True

    def test_set_member_as_moderator(self):
        member = User(username='Member', email='member@test.com', crypted_password='mipass')
        self.organization.members.append(member)
        self.organization.moderators.append(member)
        assert len(self.organization.members) == 1
        assert len(self.organization.moderators) == 1
        assert self.organization.is_moderator(member) is True

    def test_generate_invite_token(self):
        user = User(username='Member', email='member@test.com', crypted_password='mipass')
        token = self.organization.invite_user(user)
        assert token != ''

    def test_is_valid_token(self):
        user = User(username='Member', email='member@test.com', crypted_password='mipass')
        token = self.organization.invite_user(user)
        self.organization.pending_invitations[token] = user
        assert self.organization.is_valid_token(token)

    def test_accept_invitation_invalid_token(self):
        token = 'a token'
        response = self.organization.is_valid_token(token)
        assert not response
