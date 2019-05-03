const webpack = require('webpack');
const resolve = require('path').resolve;

const config = {
  entry: {
    login: __dirname + '/app/static/scripts/login.jsx'
  },
  output: {
    path: resolve('app/static'),
    filename: '[name].js',
    publicPath: resolve('app/static')
  },
  resolve: {
    extensions: ['.js','.jsx','.css']
  },
  module: {
    rules: [{
      test: /\.jsx?/,
      loader: 'babel-loader',
      exclude: /node_modules/,
    }]
  }
};

module.exports = config;
