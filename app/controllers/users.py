from os import environ
from http import HTTPStatus
from flask import Blueprint, request, abort, jsonify, current_app
from cryptography.fernet import Fernet

try:
    from ..models.user import User
except:
    from models.user import User

users = Blueprint('users', __name__)

@users.route('/register', methods=['POST'])
def register():
    data = request.get_json(force=True)
    username = data['username']
    current_app.logger.debug('The username is: ' + username)
    email = data['email']
    current_app.logger.debug('The email is: ' + email)
    password = data['password']
    password_confirmation = data['password_confirmation']

    if len(password) < 5:
        current_app.logger.info('The password is too short. It must have at least five characters')
        abort(HTTPStatus.BAD_REQUEST)

    if password != password_confirmation:
        current_app.logger.info('The password and the confirmation are not the same')
        abort(HTTPStatus.BAD_REQUEST)

    try:
        cipher_suite = Fernet(environ['CRYPT_KEY'].encode())
        crypted_password = cipher_suite.encrypt(password.encode())
    except KeyError:
        crypted_password = password

    new_user = User(username=username, email=email, crypted_password=crypted_password)

    try:
        new_user.save()
        current_app.logger.info('The user has been created')
    except:
        abort(HTTPStatus.BAD_REQUEST)

    return '', HTTPStatus.CREATED

@users.route('/profile', methods=['POST'])
def profile():
    data = request.get_json(force=True)
    username = data['username']
    current_app.logger.debug('The username is: ' + username)
    first_name = data['first_name']
    current_app.logger.debug('The first name is: ' + first_name)
    last_name = data['last_name']
    current_app.logger.debug('The last name is: ' + last_name)

    user = User.objects(username = username)
    if not user.count():
        current_app.logger.info('The user does not exist')
        abort(HTTPStatus.BAD_REQUEST)

    user.update_one(first_name = first_name, last_name = last_name)
    current_app.logger.info('The profile has been updated')
    return '', HTTPStatus.OK

@users.route('/profile/<username>', methods = ['GET'])
def get_profile(username):
    try:
        user = User.objects.get(username = username)
    except:
        current_app.logger.info('The user does not exist')
        abort(HTTPStatus.BAD_REQUEST)

    return jsonify(first_name = user.first_name, last_name = user.last_name), HTTPStatus.OK
