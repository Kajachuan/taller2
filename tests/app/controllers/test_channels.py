from http import HTTPStatus
from taller2.app.app import app

client = app.test_client()

class TestChannelsControllers(object):
    def setup_class(cls):
        client.post('/register', data='{"username": "IronMan", "email": "user@test.com",\
                                        "password": "mipass", "password_confirmation": "mipass"}')
        client.post('/login', data='{"username": "IronMan", "password": "mipass"}')
        client.post('/organization', data = '{"name" : "Avengers"}')

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
