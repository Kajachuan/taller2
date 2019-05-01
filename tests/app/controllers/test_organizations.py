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

    #WIP
    def accept_invitation(self):
        response = client.post('/organization/Taller2/invite', data = '{"username" : "IronMan" }')
        token = response.get_json()['token']
        json_token = '{"token" : "'+token+'" }'
        response = client.post('/organization/Taller2/accept-invitation', data = json_token)
        assert response.status_code == HTTPStatus.OK
        response = client.get('/organization/Taller2/members')
        assert 'IronMan' in response.get_json()['members']

    def test_accept_invitation_invalid_token(self):
        json_token = '{"token" : "the-token-2019" }'
        response = client.post('/organization/Taller2/accept-invitation', data = json_token)
        assert response.status_code == HTTPStatus.BAD_REQUEST
