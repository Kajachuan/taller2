from http import HTTPStatus
from taller2.app.app import app

client = app.test_client()

client.post('/register', data='{"username": "IronMan", "email": "tony@stark.com",\
                              "password": "mipass", "password_confirmation": "mipass"}')

class TestUsersController(object):
    def test_new_user(self):
        response = client.post('/register',
                               data='{"username": "MiNombre", "email": "user@test.com",\
                                      "password": "mipass", "password_confirmation": "mipass"}')
        assert response.status_code == HTTPStatus.CREATED

    def test_wrong_password_confirmation(self):
        response = client.post('/register',
                               data='{"username": "MiNombre", "email": "user@test.com",\
                                      "password": "mipass", "password_confirmation": "otropass"}')
        assert response.status_code == HTTPStatus.BAD_REQUEST

    def test_blank_username(self):
        response = client.post('/register',
                               data='{"username": "", "email": "user@test.com",\
                                      "password": "mipass", "password_confirmation": "mipass"}')
        assert response.status_code == HTTPStatus.BAD_REQUEST

    def test_invalid_email(self):
        response = client.post('/register',
                               data='{"username": "MiNombre", "email": "usertest.com",\
                                      "password": "mipass", "password_confirmation": "mipass"}')
        assert response.status_code == HTTPStatus.BAD_REQUEST

    def test_short_password(self):
        response = client.post('/register',
                               data='{"username": "MiNombre", "email": "user@test.com",\
                                      "password": "pw", "password_confirmation": "pw"}')
        assert response.status_code == HTTPStatus.BAD_REQUEST

    def test_set_profile_user_valid(self):
        response = client.post('/profile', data = '{"username" : "IronMan" , "first_name" : "Tony",\
                                        "last_name" : "Stark"}')
        assert response.status_code == HTTPStatus.OK

    def test_set_profile_user_invalid(self):
        response = client.post('/profile', data = '{"username" : "Hulk" , "first_name" : "Tony",\
                                        "last_name" : "Stark"}')
        assert response.status_code == HTTPStatus.BAD_REQUEST

    def test_get_profile(self):
        response = client.get('/profile/IronMan')
        assert response.status_code == HTTPStatus.OK
        assert response.get_json()['first_name'] == 'Tony'
        assert response.get_json()['last_name'] == 'Stark'

    def test_get_profile_invalid_user(self):
        response = client.get('/profile/Hulk')
        assert response.status_code == HTTPStatus.BAD_REQUEST

    def test_get_invitations(self):
        client.post('/login', data = '{"username" : "MiNombre", "password" : "mipass"}')
        response = client.get('/profile/MiNombre/invitations')
        assert response.status_code == HTTPStatus.OK
        assert len(response.get_json()['invitations'].keys()) == 0

    def test_get_invitations_not_my_user(self):
        client.post('/login', data = '{"username" : "MiNombre", "password" : "mipass"}')
        response = client.get('/profile/IronMan/invitations')
        assert response.status_code == HTTPStatus.FORBIDDEN

    def test_get_user_organizations_empty(self):
        client.post('/login', data = '{"username" : "MiNombre", "password" : "mipass"}')
        response = client.get('/profile/MiNombre/organizations')
        assert response.status_code == HTTPStatus.OK
        assert len(response.get_json()['organizations']) == 0

    def test_get_user_organizations_owner(self):
        client.post('/login', data = '{"username" : "IronMan", "password" : "mipass"}')
        client.post('/organization',data = '{"name" : "Avengers"}')
        response = client.get('/profile/IronMan/organizations')
        assert response.status_code == HTTPStatus.OK
        assert 'Avengers' in response.get_json()['organizations']

    def test_get_user_organizations_member(self):
        response = client.post('/organization/Avengers/invite', data = '{"username" : "MiNombre" }')
        assert response.status_code == HTTPStatus.OK
        client.post('/login', data = '{"username" : "MiNombre", "password" : "mipass"}')
        invitations = client.get('/profile/MiNombre/invitations')
        token = list(invitations.get_json()['invitations'].keys())[0]
        organization = invitations.get_json()['invitations'][token]
        response = client.post('/organization/'+organization+'/accept-invitation', data = '{"token" : "'+token+'"}')
        assert response.status_code == HTTPStatus.OK
        response = client.get('/organization/Avengers/members')
        assert 'MiNombre' in response.get_json()['members']
        response = client.get('/profile/MiNombre/organizations')
        assert response.status_code == HTTPStatus.OK
        assert 'Avengers' in response.get_json()['organizations']
