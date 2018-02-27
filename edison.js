/*
 * Blank IoT Node.js starter app.
 *
 * Use this template to start an IoT Node.js app on any supported IoT board.
 * The target board must support Node.js. It is helpful if the board includes
 * support for I/O access via the MRAA and UPM libraries.
 *
 * https://software.intel.com/en-us/xdk/docs/lp-xdk-iot
 */


// keep /*jslint and /*jshint lines for proper jshinting and jslinting
// see http://www.jslint.com/help.html and http://jshint.com/docs
/* jslint node:true */
/* jshint unused:true */

"use strict" ;

const mqtt = require("mqtt");

var mraa = require("mraa") ;
var beepPin = new mraa.Gpio(2);
beepPin.dir(mraa.DIR_OUT);

//MQTT
var client = mqtt.connect("mqtt://192.168.43.125:1883");
client.subscribe('status');

function temp()
{
    beepPin.write(0)
    var mraa= require('mraa'); 
    var B=4275;
    var R0=100000;
    var tempPin = new mraa.Aio(3); 
    
    var a=tempPin.read();
    var R=1023/a-1; 
    R=R0*R;
    var temperature=1/(Math.log(R/100000)/B+1/298.15)-273.15;
    temperature = +temperature.toFixed(2);
    
    client.publish('temperature', temperature.toString())
    console.log(temperature); 
    setTimeout(temp,1000); 
}

client.on('status', function (topic, message) {
var m = message.toString();
    console.log(m);
    beepPin.write(1);
});



temp()
 
