from os import environ
from http import HTTPStatus
from flask import Blueprint, request, abort, session, current_app, jsonify
from ..models.organization import Organization
from ..models.user import User

organizations = Blueprint('organizations', __name__)

def get_organization(organization_name):
    try:
        organization = Organization.objects.get(organization_name = organization_name)
    except Organization.DoesNotExist:
        abort(jsonify(msg = 'Organization does not exist'),HTTPStatus.BAD_REQUEST)
    return organization

@organizations.route('/organization', methods=['POST'])
def create():
    data = request.get_json(force=True)
    organization_name = data['name']
    current_app.logger.debug('The organization name is: ' + organization_name)
    try:
        owner = User.objects.get(username = session['username'])
    except KeyError:
        return jsonify(msg = 'You have to be logged'),HTTPStatus.UNAUTHORIZED

    new_organization = Organization(organization_name = organization_name, owner = owner)

    try:
        new_organization.save()
        current_app.logger.debug('Organization created')
    except:
        current_app.logger.debug('Failed to create Organization')
        return jsonify(msg = 'Failed to create'),HTTPStatus.BAD_REQUEST
    new_organization.update(push__members = owner)
    return jsonify(msg = 'Organization created'),HTTPStatus.CREATED

@organizations.route('/organization/<organization_name>/invite', methods=['POST'])
def send_invitation(organization_name):
    data = request.get_json(force = True)
    username = data['username']
    try:
        organization = Organization.objects.get(organization_name = organization_name)
    except Organization.DoesNotExist:
        return jsonify(msg = 'Organization does not exist'),HTTPStatus.BAD_REQUEST
    try:
        user = User.objects.get(username = username)
    except User.DoesNotExist:
        return jsonify(msg = 'User does not exist'),HTTPStatus.BAD_REQUEST
    token = organization.invite_user(user)
    organization.update(**{'set__pending_invitations__' + token: user.username})
    return jsonify(msg = 'Sent invitation', token = token),HTTPStatus.OK

@organizations.route('/organization/<organization_name>/accept-invitation', methods=['POST'])
def accept(organization_name):
    data = request.get_json(force = True)
    token = data['token']
    organization = Organization.objects.get(organization_name = organization_name)
    if organization.is_valid_token(token):
        username = organization.pending_invitations[token]
        user = User.objects.get(username = username)
        organization.update(push__members = user)
        return jsonify(msg = 'Member added'),HTTPStatus.OK
    return jsonify(msg = 'Invalid Token'),HTTPStatus.BAD_REQUEST

@organizations.route('/organization/<organization_name>/members', methods=['GET'])
def return_members(organization_name):
    try:
        organization = Organization.objects.get(organization_name = organization_name)
    except Organization.DoesNotExist:
        return jsonify(msg = 'Inexistent organization'),HTTPStatus.BAD_REQUEST
    usernames = [member.username for member in organization.members]
    return jsonify(members = usernames),HTTPStatus.OK
