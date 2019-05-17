from http import HTTPStatus
from taller2.app.app import app

client = app.test_client()

client.post('/register', data='{"username": "orgacreator", "email": "user@test.com",\
                                "password": "mipass", "password_confirmation": "mipass"}')
client.post('/login', data='{"username": "orgacreator", "password": "mipass"}')

class TestOrganizationsController(object):
    def test_correct_creation_of_organization(self):
        response = client.post('/organization', data = '{"name" : "Taller2"}')
        assert response.status_code == HTTPStatus.CREATED

    def test_get_default_info_organization(self):
        response = client.get('/organization/Taller2')
        assert response.status_code == HTTPStatus.OK
        info = response.get_json()
        assert info['name'] == 'Taller2'
        assert info['owner'] == 'orgacreator'
        assert info['ubication'] == 'Not Specified'
        assert info['image_link'] == None
        assert info['description'] == 'Organization Information'
        assert info['welcome_message'] == 'Welcome'

    def test_change_some_info_organization(self):
        response = client.post('/organization/Taller2',
                                data = '{"ubication":"Argentina", "welcome_message" : "Hello everyone"}')
        assert response.status_code == HTTPStatus.OK
        response = client.get('/organization/Taller2')
        assert response.status_code == HTTPStatus.OK
        info = response.get_json()
        assert info['name'] == 'Taller2'
        assert info['owner'] == 'orgacreator'
        assert info['ubication'] == 'Argentina'
        assert info['image_link'] == None
        assert info['description'] == 'Organization Information'
        assert info['welcome_message'] == 'Hello everyone'

    def test_fail_change_info_not_owner(self):
        client.delete('/logout')
        response = client.post('/organization/Taller2',
                                data = '{"ubication":"Argentina", "welcome_message" : "Hello everyone"}')
        assert response.status_code == HTTPStatus.UNAUTHORIZED
        client.post('/login', data='{"username": "IronMan", "password": "mipass"}')
        response = client.post('/organization/Taller2',
                                data = '{"ubication":"Argentina", "welcome_message" : "Hello everyone"}')
        assert response.status_code == HTTPStatus.FORBIDDEN
        client.post('/login', data='{"username": "orgacreator", "password": "mipass"}')

    def test_missing_name(self):
        response = client.post('/organization')
        assert response.status_code == HTTPStatus.BAD_REQUEST

    def test_user_not_logged(self):
        client.delete('/logout')
        response = client.post('/organization', data = '{"name" : "Taller2"}')
        assert response.status_code == HTTPStatus.UNAUTHORIZED
        client.post('/login', data='{"username": "orgacreator", "password": "mipass"}')

    def test_get_members(self):
        response = client.get('/organization/Taller2/members')
        assert response.status_code == HTTPStatus.OK
        assert 'orgacreator' in response.get_json()['members']

    def test_get_members_inexistent_organization(self):
        response = client.get('/organization/OrgaDatos/members')
        assert response.status_code == HTTPStatus.BAD_REQUEST

    def test_invite_user(self):
        organization_name = 'Taller2'
        response = client.post('/organization/'+organization_name+'/invite', data = '{"username" : "IronMan" }')
        assert response.status_code == HTTPStatus.OK

    def test_invite_not_registered_user(self):
        organization_name = 'Taller2'
        response = client.post('/organization/'+organization_name+'/invite', data = '{"username" : "Grynberg" }')
        assert response.status_code == HTTPStatus.BAD_REQUEST

    def test_invite_user_inexistent_organization(self):
        organization_name = 'OrgaDatos'
        response = client.post('/organization/'+organization_name+'/invite', data = '{"username" : "IronMan" }')
        assert response.status_code == HTTPStatus.BAD_REQUEST

    def test_accept_invitation(self):
        response = client.post('/organization/Taller2/invite', data = '{"username" : "IronMan" }')
        assert response.status_code == HTTPStatus.OK
        client.post('/login', data = '{"username" : "IronMan", "password" : "mipass"}')
        invitations = client.get('/profile/IronMan/invitations')
        token = list(invitations.get_json()['invitations'].keys())[0]
        organization = invitations.get_json()['invitations'][token]
        response = client.post('/organization/'+organization+'/accept-invitation', data = '{"token" : "'+token+'"}')
        assert response.status_code == HTTPStatus.OK
        response = client.get('/organization/Taller2/members')
        assert 'IronMan' in response.get_json()['members']

    def test_invite_user_that_is_member(self):
        response = client.post('/organization/Taller2/invite', data = '{"username" : "IronMan"}')
        assert response.status_code == HTTPStatus.BAD_REQUEST
        assert response.get_json()['message'] == 'User is already a member'

    def test_accept_invitation_invalid_token(self):
        json_token = '{"token" : "the-token-2019" }'
        response = client.post('/organization/Taller2/accept-invitation', data = json_token)
        assert response.status_code == HTTPStatus.BAD_REQUEST

    def test_delete_member(self):
        client.post('/login', data = '{"username" : "orgacreator", "password" : "mipass"}')
        response = client.delete('/organization/Taller2/members', data = '{"username" : "IronMan"}')
        assert response.status_code == HTTPStatus.OK
        response = client.get('/organization/Taller2/members')
        assert not "IronMan" in response.get_json()['members']

    def test_delete_not_member(self):
        response = client.delete('/organization/Taller2/members', data = '{"username" : "IronMan"}')
        assert response.status_code == HTTPStatus.BAD_REQUEST
        assert response.get_json()['message'] == 'User is not member'

    def test_delete_member_only_owner(self):
        client.post('/login', data = '{"username" : "testlogin", "password" : "mipass"}')
        response = client.delete('/organization/Taller2/members', data = '{"username" : "orgacreator"}')
        assert response.status_code == HTTPStatus.FORBIDDEN
        assert response.get_json()['message'] == 'Only owner can delete a member'

    def test_delete_member_inexistent_organization(self):
        response = client.delete('/organization/OrgaDatos/members', data = '{"username" : "orgacreator"}')
        assert response.status_code == HTTPStatus.BAD_REQUEST
        assert response.get_json()['message'] == 'Organization does not exist'

    def test_get_moderators(self):
        response = client.get('/organization/Taller2/moderators')
        assert response.status_code == HTTPStatus.OK
        assert len(response.get_json()['moderators']) == 0

    def test_upgrade_to_moderatoror_not_member(self):
        client.post('/login', data = '{"username" : "orgacreator", "password" : "mipass"}')
        response = client.post('/organization/Taller2/moderators', data = '{"username":"Thanos"}')
        assert response.status_code == HTTPStatus.BAD_REQUEST
        response = client.get('/organization/Taller2/moderators')
        assert response.status_code == HTTPStatus.OK
        assert response.get_json()['moderators'] == []

    def test_upgrade_member_to_moderator(self):
        client.post('/login', data = '{"username" : "orgacreator", "password" : "mipass"}')
        response = client.post('/organization/Taller2/invite', data = '{"username" : "IronMan" }')
        assert response.status_code == HTTPStatus.OK
        client.post('/login', data = '{"username" : "IronMan", "password" : "mipass"}')
        invitations = client.get('/profile/IronMan/invitations')
        token = list(invitations.get_json()['invitations'].keys())[-1]
        organization = invitations.get_json()['invitations'][token]
        response = client.post('/organization/'+organization+'/accept-invitation', data = '{"token" : "'+token+'"}')
        assert response.status_code == HTTPStatus.OK
        response = client.get('/organization/Taller2/members')
        assert 'IronMan' in response.get_json()['members']
        response = client.post('/organization/Taller2/moderators', data = '{"username":"IronMan"}')
        assert response.status_code == HTTPStatus.OK
        response = client.get('/organization/Taller2/moderators')
        assert response.status_code == HTTPStatus.OK
        assert len(response.get_json()['moderators']) == 1
        assert "IronMan" in response.get_json()['moderators']

    def test_delete_moderator(self):
        client.post('/login', data = '{"username" : "orgacreator", "password" : "mipass"}')
        response = client.delete('/organization/Taller2/moderators', data = '{"username" : "IronMan" }')
        assert response.status_code == HTTPStatus.OK
        response = client.get('/organization/Taller2/moderators')
        assert response.status_code == HTTPStatus.OK
        assert len(response.get_json()['moderators']) == 0
        assert not "IronMan" in response.get_json()['moderators']

    def test_delete_not_moderator(self):
        client.post('/login', data = '{"username" : "orgacreator", "password" : "mipass"}')
        response = client.delete('/organization/Taller2/moderators', data = '{"username" : "IronMan" }')
        assert response.status_code == HTTPStatus.BAD_REQUEST
        response = client.get('/organization/Taller2/moderators')
        assert response.status_code == HTTPStatus.OK
        assert response.get_json()['moderators'] == []
