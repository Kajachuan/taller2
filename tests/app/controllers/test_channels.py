from http import HTTPStatus
from taller2.app.app import app

client = app.test_client()

class TestChannelsControllers(object):
    def setup_class(cls):
        client.post('/register', data='{"username": "IronMan", "email": "iron@test.com",\
                                        "password": "mipass", "password_confirmation": "mipass"}')
        client.post('/register', data='{"username": "Thor", "email": "thor@test.com",\
                                        "password": "mipass", "password_confirmation": "mipass"}')
        client.post('/login', data='{"username": "IronMan", "password": "mipass"}')
        client.post('/organization', data = '{"name" : "Avengers"}')
        client.post('/organization/Avengers/invite', data = '{"username" : "Thor" }')
        client.post('/login', data = '{"username" : "Thor", "password" : "mipass"}')
        invitations = client.get('/profile/Thor/invitations')
        token = list(invitations.get_json()['invitations'].keys())[0]
        organization = invitations.get_json()['invitations'][token]
        response = client.post('/organization/'+organization+'/accept-invitation', data = '{"token" : "'+token+'"}')
        response = client.get('/organization/Avengers/members')
        assert 'Thor' in response.get_json()['members']
        client.post('/login', data='{"username": "IronMan", "password": "mipass"}')

    def test_get_channels_empty(self):
        response = client.get('/organization/Avengers/channels')
        assert response.get_json()['channels'] == []
        assert response.status_code == HTTPStatus.OK

    def test_create_channel(self):
        response = client.post('/organization/Avengers/channels', data = '{"name" : "EndGame", "private" : "True"}')
        assert response.status_code == HTTPStatus.CREATED
        response = client.get('/organization/Avengers/channels')
        assert 'EndGame' in response.get_json()['channels']
        assert len(response.get_json()['channels']) == 1

    def test_cant_create_same_channel_in_organization(self):
        response = client.post('/organization/Avengers/channels', data = '{"name" : "EndGame", "private" : "True"}')
        assert response.status_code == HTTPStatus.BAD_REQUEST
        response = client.get('/organization/Avengers/channels')
        assert len(response.get_json()['channels']) == 1

    def test_get_channel_members(self):
        response = client.get('/organization/Avengers/EndGame/members')
        assert response.get_json()['members'] == ['IronMan']

    def test_add_member_not_in_organization(self):
        response = client.post('/organization/Avengers/EndGame/members', data = '{"name" : "Superman"}')
        assert response.status_code == HTTPStatus.BAD_REQUEST

    def test_add_member_ok(self):
        response = client.post('/organization/Avengers/EndGame/members', data = '{"name" : "Thor"}')
        assert response.status_code == HTTPStatus.OK
        response = client.get('/organization/Avengers/EndGame/members')
        assert response.get_json()['members'] == ['IronMan', 'Thor']
