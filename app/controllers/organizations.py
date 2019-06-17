from os import environ
from http import HTTPStatus
from datetime import datetime
from flask import Blueprint, request, abort, session, current_app, jsonify
from ..models.organization import Organization
from ..models.user import User
from ..models.message import Message
from ..decorators.no_ban_required import no_ban_required

organizations = Blueprint('organizations', __name__)

@organizations.app_errorhandler(Organization.DoesNotExist)
@organizations.app_errorhandler(User.DoesNotExist)
def inexistent_organization_handler(e):
    error_messages = {'Organization matching query does not exist.' : 'Organization does not exist',
                      'User matching query does not exist.' : 'User does not exist'}
    current_app.logger.debug('Error: %s',error_messages[e.args[0]])
    return jsonify(message = error_messages[e.args[0]]), HTTPStatus.BAD_REQUEST

@organizations.route('/organization', methods=['POST'])
@no_ban_required
def create():
    data = request.get_json(force=True)
    organization_name = data['name']
    current_app.logger.debug('The organization name is: ' + organization_name)

    owner = User.objects.get(username = session['username'])
    new_organization = Organization(organization_name = organization_name, owner = owner, creation_date = datetime.now())

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
@no_ban_required
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
@no_ban_required
def change_info(organization_name):
    organization = Organization.objects.get(organization_name = organization_name)
    username = session['username']
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
@no_ban_required
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
@no_ban_required
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
@no_ban_required
def return_members(organization_name):
    organization = Organization.objects.get(organization_name = organization_name)
    usernames = [member.username for member in organization.members]
    return jsonify(members = usernames),HTTPStatus.OK

@organizations.route('/organization/<organization_name>/members', methods=['DELETE'])
@no_ban_required
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
@no_ban_required
def get_moderators(organization_name):
    organization = Organization.objects.get(organization_name = organization_name)
    usernames = [member.username for member in organization.moderators]
    return jsonify(moderators = usernames),HTTPStatus.OK

@organizations.route('/organization/<organization_name>/moderators', methods=['POST'])
@no_ban_required
def upgrade_to_moderator(organization_name):
    organization = Organization.objects.get(organization_name = organization_name)
    username = request.get_json(force = True)['username']
    user = User.objects.get(username = username)
    if not organization.is_member(user):
        return jsonify(message = 'User is not member'), HTTPStatus.BAD_REQUEST
    organization.update(push__moderators = user)
    return jsonify(message = 'member is now a moderator'), HTTPStatus.OK

@organizations.route('/organization/<organization_name>/moderators', methods=['DELETE'])
@no_ban_required
def delete_moderator(organization_name):
    organization = Organization.objects.get(organization_name = organization_name)
    username = request.get_json(force = True)['username']
    user = User.objects.get(username = username)
    if not organization.is_moderator(user):
        return jsonify(message = 'User is not moderator'), HTTPStatus.BAD_REQUEST
    organization.update(pull__moderators = user)
    return jsonify(message = 'member is not a moderator anymore'), HTTPStatus.OK

#channels

@organizations.route('/organization/<organization_name>/channels', methods=['GET'])
@no_ban_required
def get_channels(organization_name):
    organization = Organization.objects.get(organization_name = organization_name)
    channels = [channel.channel_name for channel in organization.channels if session['username'] in channel.members]
    return jsonify(channels = channels), HTTPStatus.OK

@organizations.route('/organization/<organization_name>/channels', methods=['POST'])
@no_ban_required
def create_channel(organization_name):
    data = request.get_json(force = True)
    channel_name = data['name']
    owner = session['username']
    privado = True if data['privado'] == "True" else False
    if Organization.create_channel(organization_name, channel_name, owner, privado):
        return '',HTTPStatus.CREATED
    return '',HTTPStatus.BAD_REQUEST

@organizations.route('/organization/<organization_name>/<channel_name>/members', methods=['GET'])
@no_ban_required
def get_channel_members(organization_name, channel_name):
    members = Organization.get_channel_members(organization_name, channel_name)
    return jsonify(members = members), HTTPStatus.OK

@organizations.route('/organization/<organization_name>/<channel_name>/members', methods=['POST'])
@no_ban_required
def add_member_to_channel(organization_name, channel_name):
    data = request.get_json(force = True)
    member = data['name']
    if not Organization.has_member(organization_name, member):
        return jsonify(message = 'User is not member'), HTTPStatus.BAD_REQUEST
    Organization.add_member_to_channel(organization_name, channel_name, member)
    return '', HTTPStatus.OK

@organizations.route('/organization/<organization_name>/<channel_name>/messages', methods=['POST'])
@no_ban_required
def get_n_channel_messages(organization_name, channel_name):
    data = request.get_json(force = True)
    channel = Organization.get_channel(organization_name, channel_name)
    messages = channel.get_messages(int(data['init']), int(data['end']))
    list_of_msg = [(message.timestamp,message.sender,message.message) for message in messages]
    return jsonify(messages = list_of_msg), HTTPStatus.OK

@organizations.route('/organization/<organization_name>/<channel_name>/message', methods=['POST'])
@no_ban_required
def send_message(organization_name, channel_name):
    data = request.get_json(force = True)
    channel = Organization.get_channel(organization_name,channel_name)
    message = Message(message = data['message'], sender = data['sender'], timestamp = data['timestamp'], creation_date = datetime.now())
    message.save()
    channel.update(push__messages = message)
    return '',HTTPStatus.OK
