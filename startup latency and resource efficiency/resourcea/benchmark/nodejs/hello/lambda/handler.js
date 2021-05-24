'use strict';

module.exports.hello = (event, context, callback) => {
  const response = {
    statusCode: 200,
    body: "Hello Serverless"
  };

  callback(null, response);
};
