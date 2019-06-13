from requests import get
from base64 import b64encode
from os import environ, path
from facebook import GraphAPI

class MockFacebookAPI(object):
    def __init__(self, access_token):
        if access_token != 'correct_token':
            raise Exception('Invalid OAuth access token')

    def get_object(self, id, **args):
        if 'type' in args.keys():
            image_path = '../static/img/default_image.png'
            image_path = path.join(path.dirname(__file__), image_path)
            image = open(path.abspath(image_path), 'rb').read()
            return {'data': image}
        if args['fields'] == 'id':
            return {'id': '12345'}
        else:
            return {'id': '12345', 'first_name': 'MyName',
                    'last_name': 'MyLastName', 'email': 'test@user.com'}

class FacebookAPI(object):
    def __init__(self, token):
        if environ['FLASK_ENV'] == 'production':
            self.api = GraphAPI(access_token=token, version='3.1')
        else:
            self.api = MockFacebookAPI(access_token=token)

    def get_id(self):
        profile = self.api.get_object(id='me', fields='id')
        return profile['id']

    def get_profile(self):
        info = self.api.get_object(id='me', fields='id,first_name,last_name,email')
        image = self.api.get_object(id=info['id'] + '/picture', type='large')
        profile = {}
        profile['id'] = info['id']
        profile['first_name'] = info['first_name']
        profile['last_name'] = info['last_name']
        profile['email'] = info['email']
        profile['encoded_image'] = b64encode(image['data']).decode()
        return profile
