!function(r){function e(e){for(var n,s,a=e[0],l=e[1],u=e[2],c=0,f=[];c<a.length;c++)s=a[c],o[s]&&f.push(o[s][0]),o[s]=0;for(n in l)Object.prototype.hasOwnProperty.call(l,n)&&(r[n]=l[n]);for(d&&d(e);f.length;)f.shift()();return i.push.apply(i,u||[]),t()}function t(){for(var r,e=0;e<i.length;e++){for(var t=i[e],n=!0,a=1;a<t.length;a++){var l=t[a];0!==o[l]&&(n=!1)}n&&(i.splice(e--,1),r=s(s.s=t[0]))}return r}var n={},o={3:0},i=[];function s(e){if(n[e])return n[e].exports;var t=n[e]={i:e,l:!1,exports:{}};return r[e].call(t.exports,t,t.exports,s),t.l=!0,t.exports}s.m=r,s.c=n,s.d=function(r,e,t){s.o(r,e)||Object.defineProperty(r,e,{enumerable:!0,get:t})},s.r=function(r){"undefined"!=typeof Symbol&&Symbol.toStringTag&&Object.defineProperty(r,Symbol.toStringTag,{value:"Module"}),Object.defineProperty(r,"__esModule",{value:!0})},s.t=function(r,e){if(1&e&&(r=s(r)),8&e)return r;if(4&e&&"object"==typeof r&&r&&r.__esModule)return r;var t=Object.create(null);if(s.r(t),Object.defineProperty(t,"default",{enumerable:!0,value:r}),2&e&&"string"!=typeof r)for(var n in r)s.d(t,n,function(e){return r[e]}.bind(null,n));return t},s.n=function(r){var e=r&&r.__esModule?function(){return r.default}:function(){return r};return s.d(e,"a",e),e},s.o=function(r,e){return Object.prototype.hasOwnProperty.call(r,e)},s.p="./";var a=window.webpackJsonp=window.webpackJsonp||[],l=a.push.bind(a);a.push=e,a=a.slice();for(var u=0;u<a.length;u++)e(a[u]);var d=l;i.push([10,0,1]),t()}({10:function(r,e,t){"use strict";(function(r){t(3),t(21),t(36);var e=t(4),n=t(5);r(".loginType").click(function(){var e=r(this).attr("data-type");r(".password-login, .verify-login").hide(),r("."+e+"-login").show()}),r(".toggleForm").click(function(){var e=r(this).attr("data-type");r(".register-container, .login-container").hide(),r("."+e+"-container").show()});var o=void 0,i=/^1[3|4|5|7|8|9][0-9]{9}$/;r(".sms-btn").click(function(){var t=r(this),s=t.parents(".login-box").find(".userId").val()||t.parents(".register-form").find(".userId").val();if(!s)return n.error("手机号码不能为空！");if(11!==s.length||!i.test(s))return n.error("您输入的手机号码格式不正确");r.ajax({url:e+"/user/sms",type:"post",data:{mobileNumber:s.toString()},success:function(r){if(!r.success)return n.error(r.errorMessage),t.html("获取验证码").attr("disabled",!1),clearInterval(o),o=null,!1}}).fail(function(r){});var a=0;return o=setInterval(function(){if(60===a)return t.html("获取验证码").attr("disabled",!1),clearInterval(o),o=null,!1;t.html("重新发送("+(60-a)+"s)").attr("disabled",!0),a++},1e3),!1}),r(".register-sms-btn").click(function(){var t=r(this),s=t.parents(".login-box").find(".userId").val()||t.parents(".register-form").find(".userId").val();if(!s)return n.error("手机号码不能为空！");if(11!==s.length||!i.test(s))return n.error("您输入的手机号码格式不正确");r.ajax({url:e+"/user/registerSms",type:"post",data:{mobileNumber:s.toString()},success:function(r){if(!r.success)return n.error(r.errorMessage),t.html("获取验证码").attr("disabled",!1),clearInterval(o),o=null,!1}}).fail(function(r){});var a=0;return o=setInterval(function(){if(60===a)return t.html("获取验证码").attr("disabled",!1),clearInterval(o),o=null,!1;t.html("重新发送("+(60-a)+"s)").attr("disabled",!0),a++},1e3),!1});var s=!1,a="",l="";document.cookie&&(a=document.cookie.split(";")[0].split("=")[1],l=document.cookie.split(";")[1].split("=")[1]),a&&l&&(s=!s,r(".record").prop("checked",!0).parents(".login-box").find(".userId").val(a).end().find(".password").val(l)),r(".login-btn").click(function(){var t=r(this),o=t.parents(".login-box").find(".userId").val().toString();if(!o)return n.error("手机号码不能为空！");if(11!==o.length||!i.test(o))return n.error("您输入的手机号码格式不正确！");if("verify"==t.attr("data-type")){var u=t.parents(".login-box").find(".smsCode").val().toString();if(!u)return n.error("验证码不能为空！");r.post(e+"/user/login/mobile",{mobile:o,smsCode:u},function(){sessionStorage.setItem("login",!0),window.location.href="/index.html"}).fail(function(r){n.error(r.responseJSON)})}else{var d=t.parents(".login-box").find(".password").val().toString();if(!d)return n.error("密码不能为空！");r.post(e+"/user/login",{username:o,password:d},function(e){if(r(".record").prop("checked")&&(!s||a!==o||l!==d)){var t=new Date;t.setDate(t.getDate()+30),document.cookie="username="+o+";expires="+t.toGMTString(),document.cookie="password="+d+";expires="+t.toGMTString()}sessionStorage.setItem("login",!0),window.location.href="/index.html"}).fail(function(r){n.error(r.responseJSON)})}}),r(".register-btn").click(function(){var t=r(this).parents(".register-form"),o=t.find(".userId").val().toString(),s=t.find(".password").val().toString(),a=t.find(".username").val().toString(),l=t.find(".email").val().toString(),u=t.find(".hospital").val().toString(),d=t.find(".department").val().toString(),c=t.find(".title").val().toString(),f=t.find(".smsCode").val();return""==u?(t.find(".hospital").css("border-color","red"),n.error("所属医院不能为空！")):""==a?(t.find(".username").css("border-color","red"),n.error("联系人不能为空！")):""==o?(t.find(".userId").css("border-color","red"),n.error("联系方式不能为空！")):11===o.length&&i.test(o)?""==f?(t.find(".smsCode").css("border-color","red"),n.error("验证码不能为空！")):""==s?(t.find(".password").css("border-color","red"),n.error("密码不能为空！")):""!=u&&""!=a&&""!=o&&""!=s?t.find(".confirm").prop("checked")?void r.ajax({url:e+"/user/register",type:"post",data:{mobile:o,password:s,username:a,email:l,hospital:u,department:d,title:c,smsCode:f}}).done(function(r){r.errorMessage?n.error(r.errorMessage):(n.success("注册成功！",500,2800),setTimeout(function(){return window.location.reload()},3e3))}).fail(function(r){n.error(r.responseText)}):alert("请阅读并接受《用户协议》！"):void 0:(t.find(".userId").css("border-color","red"),n.error("您输入的手机号码格式不正确！"))})}).call(this,t(0))},36:function(r,e){}});