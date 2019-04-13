const webpack = require('webpack');
const resolve = require('path').resolve;

const config = {
  entry: __dirname + '/app/static/scripts/index.jsx',
  output: {
    path: resolve('app/static'),
    filename: 'bundle.js',
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
