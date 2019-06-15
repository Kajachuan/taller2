from http import HTTPStatus
from taller2.app.app import app
import datetime as dt

client = app.test_client()

class TestChannelsControllers(object):
    def setup_class(cls):
        client.post('/register', data='{"username": "IronMan", "email": "iron@test.com",\
                                        "password": "mipass", "password_confirmation": "mipass"}')
        client.post('/register', data='{"username": "Thor", "email": "thor@test.com",\
                                        "password": "mipass", "password_confirmation": "mipass"}')
        client.post('/register', data='{"username": "Thanos", "email": "thanos@test.com",\
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

    def test_get_user_channels_empty(self):
        response = client.get('/organization/Avengers/channels')
        assert response.get_json()['channels'] == []
        assert response.status_code == HTTPStatus.OK

    def test_create_channel(self):
        response = client.post('/organization/Avengers/channels', data = '{"name" : "EndGame", "privado" : "True"}')
        assert response.status_code == HTTPStatus.CREATED
        response = client.get('/organization/Avengers/channels')
        assert 'EndGame' in response.get_json()['channels']
        assert len(response.get_json()['channels']) == 1

    def test_cant_create_same_channel_in_organization(self):
        response = client.post('/organization/Avengers/channels', data = '{"name" : "EndGame", "privado" : "True"}')
        assert response.status_code == HTTPStatus.BAD_REQUEST
        response = client.get('/organization/Avengers/channels')
        assert len(response.get_json()['channels']) == 1

    def test_get_user_channels_in_organization(self):
        client.post('/login', data = '{"username" : "Thor", "password" : "mipass"}')
        client.post('/organization/Avengers/channels', data = '{"name" : "Asgard", "privado" : "False"}')
        client.post('/login', data = '{"username" : "IronMan", "password" : "mipass"}')
        response = client.get('/organization/Avengers/channels')
        assert response.status_code == HTTPStatus.OK
        assert response.get_json()['channels'] == ['EndGame']

    def test_get_channel_members(self):
        response = client.get('/organization/Avengers/EndGame/members')
        assert response.get_json()['members'] == ['IronMan']

    def test_add_member_not_in_organization(self):
        response = client.post('/organization/Avengers/EndGame/members', data = '{"name" : "Thanos"}')
        assert response.status_code == HTTPStatus.BAD_REQUEST
        assert response.get_json()['message'] == 'User is not member'

    def test_add_member_ok(self):
        response = client.post('/organization/Avengers/EndGame/members', data = '{"name" : "Thor"}')
        assert response.status_code == HTTPStatus.OK
        response = client.get('/organization/Avengers/EndGame/members')
        assert response.get_json()['members'] == ['IronMan', 'Thor']

    def test_get_channels_messages_empty(self):
        response = client.post('/organization/Avengers/EndGame/messages', data = '{"init":"1", "end":"5"}')
        assert response.status_code == HTTPStatus.OK
        assert response.get_json()['messages'] == []

    def test_send_message_to_channel(self):
        data = '{"sender":"IronMan","message":"Hello1"}'
        response = client.post('/organization/Avengers/EndGame/message', data = data)
        assert response.status_code == HTTPStatus.OK

    def test_get_channels_messages(self):
        response = client.post('/organization/Avengers/EndGame/messages', data = '{"init":"1", "end":"5"}')
        assert response.status_code == HTTPStatus.OK
        message_wout_tstamp = [(message[1], message[2]) for message in response.get_json()['messages']]
        assert message_wout_tstamp == [('IronMan','Hello1')]

    def test_get_messages_by_slices(self):
        data = ['{"sender":"IronMan","message":"Hello2"}',
                '{"sender":"IronMan","message":"Hello3"}',
                '{"sender":"Thor","message":"Hello4"}',
                '{"sender":"Thor","message":"Hello5"}']
        for msg in data:
            client.post('/organization/Avengers/EndGame/message', data = msg)
        response = client.post('/organization/Avengers/EndGame/messages', data = '{"init":"1", "end":"2"}')
        message_wout_tstamp = [(message[1], message[2]) for message in response.get_json()['messages']]
        assert message_wout_tstamp == [('Thor','Hello4'),('Thor','Hello5')]
        response = client.post('/organization/Avengers/EndGame/messages', data = '{"init":"3", "end":"5"}')
        message_wout_tstamp = [(message[1], message[2]) for message in response.get_json()['messages']]
        print(message_wout_tstamp)
        assert message_wout_tstamp == [('IronMan','Hello1'),('IronMan','Hello2'),('IronMan','Hello3')]
