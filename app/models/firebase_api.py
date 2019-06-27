import firebase_admin
from firebase_admin import credentials
from firebase_admin import messaging
from os import environ
from .user import User

PRODUCTION = 'production'
CREDENTIALS_PATH = 'hypechat-647c1-firebase-adminsdk-bo1d5-77d6497801.json'
SENDER = 'sender'
MESSAGE = 'message'
TIMESTAMP = 'timestamp'
ORGANIZATION = 'organization'
CHANNEL = 'channel'
TYPE = 'type'
BOT = 'bot'
HELP_KEYS = ['help', 'info', 'mute', 'me']
INFO_KEYS = ['name', 'owner', 'description']
ME_KEYS = ['username', 'first_name', 'last_name', 'email']

class FirebaseApi(object):
    def __init__(self):
        if environ['FLASK_ENV'] == PRODUCTION:
            cred = credentials.Certificate(CREDENTIALS_PATH)
            self.default_app  = firebase_admin.initialize_app(cred)

    def send_message_to_users(self, list_username, message, organization_name, channel_name):
        if environ['FLASK_ENV'] != PRODUCTION:
            return True
        tokens = self.get_users_tokens(list_username)
        for token in tokens:
            message = messaging.Message(
                    data = {
        		SENDER : message.sender,
                MESSAGE : message.message,
                TIMESTAMP : message.timestamp,
                TYPE: message.type,
                ORGANIZATION : organization_name,
                CHANNEL : channel_name,
        		},
        	token = token,
            )
            response = messaging.send(message)
        return True

    def send_notification_to_user(self, username, organization_name, channel_name):
        user = User.objects.get(username = username)
        token = user.firebase_token
        if environ['FLASK_ENV'] == PRODUCTION:
            message = messaging.Message(
            notification=messaging.Notification(
            title='Te mencionaron en %s'%channel_name,
            body='Fuiste mencionado en %s en el canal %s'%(organization_name, channel_name),
            ),
            token = token,
            )
            messaging.send(message)

    def send_bot_response_to_user(self, username, response):
        token = User.objects.get(username = username).firebase_token
        message_response = self.parse_response(response)
        if environ['FLASK_ENV'] == PRODUCTION:
            message = messaging.Message(
                    data = {
        		SENDER : BOT,
                TYPE: BOT,
                MESSAGE : message_response,
        		},
        	token = token,
            )
            messaging.send(message)

    def get_users_tokens(self, list_username):
        tokens = []
        users = [User.objects.get(username = user_name) for user_name in list_username]
        for user in users:
            token = user.firebase_token
            if token:
                tokens.append(token)
        return tokens

    def parse_response(self, data):
        if all (key in data for key in HELP_KEYS):
            return self.get_help(data)
        elif all (key in data for key in INFO_KEYS):
            return self.get_info(data)
        elif all (key in data for key in ME_KEYS ):
            return self.get_me(data)
        else:
            return self.get_default_message(data)

    def get_help(self, data):
        response = data['help'] + '\n'
        response += data['info'] + '\n'
        response += data['mute'] + '\n'
        response += data['me']
        return response

    def get_info(self, data):
        response = 'Nombre del canal: ' + data['name'] + '\n'
        response += 'Creador: ' + data['owner'] + '\n'
        response += 'Descripcion: ' + data['description']
        return response

    def get_me(self, data):
        response = 'Nombre de usuario: ' + data['username'] + '\n'
        if data['first_name']:
            response += 'Nombre: ' + data['first_name'] + '\n'
        if data['last_name']:
            response += 'Apellido' + data['last_name'] + '\n'
        response += 'Email: ' + data['email'] + '\n'
        return response

    def get_default_message(self, data):
        response = ''
        for key, value in data.items():
            response += key + ': ' + value + '\n'
        return response
