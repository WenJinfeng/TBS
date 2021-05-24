'use strict';

exports.handler = (event, context, callback) => {
  const response = {
    statusCode: 200,
    body: {"logStreamName" : context.logStreamName}
  };

  setTimeout(function() {
    console.log(context.logStreamName)
    callback(null, response);
  }, 100);
};
