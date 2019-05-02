from os import environ
from http import HTTPStatus
from flask import Blueprint, request, session, abort, jsonify, current_app, make_response
from cryptography.fernet import Fernet
from ..models.user import User
from ..models.validators.password_validator import PasswordValidator
from ..exceptions.register_error import RegisterError

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

    try:
        PasswordValidator.validate(password, password_confirmation)
    except RegisterError as error:
        current_app.logger.info(str(error))
        abort(make_response(jsonify(message=str(error)), HTTPStatus.BAD_REQUEST))

    cipher_suite = Fernet(environ['CRYPT_KEY'].encode())
    crypted_password = cipher_suite.encrypt(password.encode())

    new_user = User(username=username, email=email, crypted_password=crypted_password)

    try:
        new_user.save()
        current_app.logger.info('The user has been created')
    except RegisterError as error:
        current_app.logger.info(str(error))
        abort(make_response(jsonify(message=str(error)), HTTPStatus.BAD_REQUEST))

    return jsonify(message='The user has been created'), HTTPStatus.CREATED

@users.route('/profile', methods=['POST'])
def profile():
    data = request.get_json(force=True)
    username = data['username']
    current_app.logger.debug('The username is: ' + username)
    first_name = data['first_name']
    current_app.logger.debug('The first name is: ' + first_name)
    last_name = data['last_name']
    current_app.logger.debug('The last name is: ' + last_name)

    user = User.objects(username=username)
    if not user.count():
        current_app.logger.info('The user does not exist')
        abort(HTTPStatus.BAD_REQUEST)

    user.update_one(first_name=first_name, last_name=last_name)
    current_app.logger.info('The profile has been updated')
    return '', HTTPStatus.OK

@users.route('/profile/<username>', methods=['GET'])
def get_profile(username):
    try:
        user = User.objects.get(username=username)
    except:
        current_app.logger.info('The user does not exist')
        abort(HTTPStatus.BAD_REQUEST)

    return jsonify(first_name=user.first_name, last_name=user.last_name), HTTPStatus.OK

@users.route('/profile/<username>/invitations', methods=['GET'])
def get_invitations(username):
    if session['username'] != username:
        return jsonify(msg = 'You are not allowed to see other user invitations'), HTTPStatus.FORBIDDEN
    user = User.objects.get(username = username)
    return jsonify(invitations = user.invitations), HTTPStatus.OK

@users.route('/profile/<username>/organizations', methods=['GET'])
def get_organizations(username):
    user = User.objects.get(username = username)
    return jsonify(organizations = user.organizations), HTTPStatus.OK
