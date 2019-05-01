from os import environ
from http import HTTPStatus
from secrets import token_urlsafe
from flask import Blueprint, request, abort, current_app, make_response, jsonify
from cryptography.fernet import Fernet
from sendgrid.helpers.mail import Mail
from ..models.user import User
from ..models.mailer import Mailer
from ..models.validators.password_validator import PasswordValidator
from ..exceptions.register_error import RegisterError

passwords = Blueprint('passwords', __name__)

@passwords.route('/recovery', methods=['POST'])
def password_recovery():
    data = request.get_json(force=True)
    username = data['username']
    current_app.logger.debug('The username is: ' + username)

    user = User.objects(username=username)
    if user.count() == 0:
        current_app.logger.info('The username does not exist')
        abort(make_response(jsonify(message='The username does not exist'), HTTPStatus.BAD_REQUEST))

    code = token_urlsafe()
    user.update_one(recovery_code=code)
    cipher_suite = Fernet(environ['CRYPT_KEY'].encode())
    token = cipher_suite.encrypt(code.encode())

    mail = Mail(
        from_email='no-reply@hypechat.com',
        to_emails='%s' % user.get(username=username).email,
        subject='Password Recovery',
        html_content='<h1>Password Recovery</h1>\
                      <p><b>Your username is:</b> %s</p>\
                      <p><b>Your token is:</b> %s</p>\
                      <p><b>This token will expire in 30 minutes</b></p>' % (username, token.decode()))
    mailer = Mailer()
    mailer.send(mail)

    current_app.logger.info('The token was sended')
    return jsonify(message='The token was sended'), HTTPStatus.OK

@passwords.route('/password', methods=['POST'])
def password_settings():
    data = request.get_json(force=True)
    username = data['username']
    current_app.logger.debug('The username is: ' + username)
    token = request.headers['Authorization']

    user_queryset = User.objects(username=username)
    if user_queryset.count() == 0:
        current_app.logger.info('The username does not exist')
        abort(make_response(jsonify(message='The username does not exist'), HTTPStatus.BAD_REQUEST))

    cipher_suite = Fernet(environ['CRYPT_KEY'].encode())

    try:
        code = cipher_suite.decrypt(token.encode(), ttl=1800)
    except:
        current_app.logger.info('The token is not valid or it has expired')
        abort(make_response(jsonify(message='The token is not valid or it has expired'), HTTPStatus.BAD_REQUEST))

    user = user_queryset.first()
    if user.recovery_code.encode() != code:
        current_app.logger.info('The token is not valid or it has expired')
        abort(make_response(jsonify(message='The token is not valid or it has expired'), HTTPStatus.BAD_REQUEST))

    new_password = data['new_password']
    new_password_confirmation = data['new_password_confirmation']

    try:
        PasswordValidator.validate(new_password, new_password_confirmation)
    except RegisterError as error:
        current_app.logger.info(str(error))
        abort(make_response(jsonify(message=str(error)), HTTPStatus.BAD_REQUEST))

    crypted_password = cipher_suite.encrypt(new_password.encode()).decode()
    user_queryset.update_one(crypted_password=crypted_password)

    current_app.logger.info('The password has been changed')
    return jsonify(message='The password has been changed'), HTTPStatus.OK
