from os import environ
from http import HTTPStatus
from flask import Blueprint, request, abort, session, current_app, jsonify
from ..models.organization import Organization
from ..models.user import User

organizations = Blueprint('organizations', __name__)

@organizations.app_errorhandler(Organization.DoesNotExist)
@organizations.app_errorhandler(User.DoesNotExist)
def inexistent_organization_handler(e):
    error_messages = {'Organization matching query does not exist.' : 'Organization does not exist',
                      'User matching query does not exist.' : 'User does not exist'}
    current_app.logger.debug('Error: %s',error_messages[e.args[0]])
    return jsonify(message = error_messages[e.args[0]]), HTTPStatus.BAD_REQUEST

@organizations.route('/organization', methods=['POST'])
def create():
    data = request.get_json(force=True)
    organization_name = data['name']
    current_app.logger.debug('The organization name is: ' + organization_name)
    try:
        owner = User.objects.get(username = session['username'])
    except KeyError:
        return jsonify(message = 'You have to be logged'),HTTPStatus.UNAUTHORIZED

    new_organization = Organization(organization_name = organization_name, owner = owner)

    try:
        new_organization.save()
        current_app.logger.debug('Organization created')
    except:
        current_app.logger.debug('Failed to create Organization')
        return jsonify(message = 'Failed to create'),HTTPStatus.BAD_REQUEST
    new_organization.update(push__members = owner)
    User.add_to_organization(owner.username, organization_name)
    return jsonify(message = 'Organization created'),HTTPStatus.CREATED

@organizations.route('/organization/<organization_name>', methods = ['GET'])
def get_info(organization_name):
    organization = Organization.objects.get(organization_name = organization_name)
    name = organization.organization_name
    owner = organization.owner.username
    ubication = organization.ubication
    image_link = organization.image_link
    description = organization.description
    welcome_message = organization.welcome_message
    return jsonify(organization = organization, name = name, owner = owner, ubication = ubication,
                   image_link = image_link, description = description, welcome_message = welcome_message),HTTPStatus.OK

@organizations.route('/organization/<organization_name>', methods = ['POST'])
def change_info(organization_name):
    organization = Organization.objects.get(organization_name = organization_name)
    try:
        username = session['username']
    except KeyError:
        return jsonify(message = 'Please log in first'), HTTPStatus.UNAUTHORIZED
    if not organization.is_owner(User.objects.get(username = username)):
        return jsonify(message = 'You are not the owner'), HTTPStatus.FORBIDDEN
    data = request.get_json(force = True)
    ubication = data.get('ubication', organization.organization_name)
    image_link = data.get('image_link', organization.image_link)
    description = data.get('description', organization.description)
    welcome_message = data.get('welcome_message', organization.welcome_message)
    organization.update(ubication = ubication)
    organization.update(image_link = image_link)
    organization.update(description = description)
    organization.update(welcome_message = welcome_message)
    return jsonify(message = 'Information changed'), HTTPStatus.OK

@organizations.route('/organization/<organization_name>/invite', methods=['POST'])
def send_invitation(organization_name):
    data = request.get_json(force = True)
    username = data['username']
    organization = Organization.objects.get(organization_name = organization_name)
    user = User.objects.get(username = username)
    if organization.is_member(user):
        return jsonify(message = 'User is already a member'),HTTPStatus.BAD_REQUEST
    token = organization.invite_user(user)
    User.invite(username, token, organization_name)
    organization.update(**{'set__pending_invitations__' + token: user.username})
    current_app.logger.debug('User %s invited to organization %s',user.username, organization_name)
    return jsonify(message = 'Sent invitation'),HTTPStatus.OK

@organizations.route('/organization/<organization_name>/accept-invitation', methods=['POST'])
def accept(organization_name):
    data = request.get_json(force = True)
    token = data['token']
    organization = Organization.objects.get(organization_name = organization_name)
    if organization.is_valid_token(token):
        username = organization.pending_invitations[token]
        user = User.objects.get(username = username)
        User.add_to_organization(username, organization_name)
        organization.update(push__members = user)
        organization.update(**{'unset__pending_invitations__' + token : user.username})
        current_app.logger.debug('User %s added to organization %s',username,organization_name)
        return jsonify(message = 'Member added'),HTTPStatus.OK
    current_app.logger.debug('User %s failed to join organization %s: invalid token',session['username'],organization_name)
    return jsonify(message = 'Invalid Token'),HTTPStatus.BAD_REQUEST

@organizations.route('/organization/<organization_name>/members', methods=['GET'])
def return_members(organization_name):
    organization = Organization.objects.get(organization_name = organization_name)
    usernames = [member.username for member in organization.members]
    return jsonify(members = usernames),HTTPStatus.OK

@organizations.route('/organization/<organization_name>/members', methods=['DELETE'])
def delete_member(organization_name):
    username = request.get_json(force = True)['username']
    organization = Organization.objects.get(organization_name = organization_name)
    user = User.objects.get(username = username)
    if not organization.is_owner(User.objects.get(username = session['username'])):
        current_app.logger.debug('Not owner tried to delete a member from %s',organization_name)
        return jsonify(message = 'Only owner can delete a member'), HTTPStatus.FORBIDDEN
    if not organization.is_member(user):
        current_app.logger.debug('Tried to remove not a member from %s',organization_name)
        return jsonify(message = 'User is not member'), HTTPStatus.BAD_REQUEST
    organization.update(pull__members__ = user)
    current_app.logger.debug('Member %s deleted from organization %s',username,organization_name)
    return jsonify(message = 'Member deleted'), HTTPStatus.OK

@organizations.route('/organization/<organization_name>/moderators', methods=['GET'])
def get_moderators(organization_name):
    organization = Organization.objects.get(organization_name = organization_name)
    usernames = [member.username for member in organization.moderators]
    return jsonify(moderators = usernames),HTTPStatus.OK

@organizations.route('/organization/<organization_name>/moderators', methods=['POST'])
def upgrade_to_moderator(organization_name):
    organization = Organization.objects.get(organization_name = organization_name)
    username = request.get_json(force = True)['username']
    user = User.objects.get(username = username)
    if not organization.is_member(user):
        return jsonify(message = 'User is not member'), HTTPStatus.BAD_REQUEST
    organization.update(push__moderators = user)
    return jsonify(message = 'member is now a moderator'), HTTPStatus.OK

@organizations.route('/organization/<organization_name>/moderators', methods=['DELETE'])
def delete_moderator(organization_name):
    organization = Organization.objects.get(organization_name = organization_name)
    username = request.get_json(force = True)['username']
    user = User.objects.get(username = username)
    if not organization.is_moderator(user):
        return jsonify(message = 'User is not moderator'), HTTPStatus.BAD_REQUEST
    organization.update(pull__moderators = user)
    return jsonify(message = 'member is not a moderator anymore'), HTTPStatus.OK
