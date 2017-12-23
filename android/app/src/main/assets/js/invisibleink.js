/*
Copyright (c) 2017 by Captain Anonymous (https://codepen.io/anon/pen/OzRPZe)
Fork of an original work by Darryl Huffman (https://codepen.io/darrylhuffman/pen/EyyNEP)

Modified by Roman Scott for the weMessage Application.

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/


(function(){
	function wrapWords(el) {
		'use strict';
		$(el).filter(':not(script)').contents().each(function () {
			if (this.nodeType === Node.ELEMENT_NODE) {
				wrapWords(this);
			} else if (this.nodeType === Node.TEXT_NODE && !this.nodeValue.match(/^\s+$/m)) {
				$(this).replaceWith($.map(this.nodeValue.split(/(\S+)/), function (w) {
					return w.match(/^\s*$/) ? document.createTextNode(w) : $('<span>', {class: 'word', text: w}).get();
				}));
			}
		});
	};


	 $('.ink').each(function(){
		$(this).html(wrapWords(this));

		$(this).find('.word').each(function(){
			inkout($(this));
			$(this).addClass('initialized');
		});
	});

	$('body').on('DOMNodeInserted', function(e) {
		if ($(e.target).is('.ink') && $(e.target).hasClass('initialized') == false) {
			setTimeout(function(){
				$(e.target).html(wrapWords(e.target));

				$(e.target).find('.word').each(function(){
					if(!$(this).hasClass('initialized')){
						inkout($(this));
						$(this).addClass('initialized');
					}
				});
				$(e.target).addClass('initialized');
			}, 100);
		}
	});


	function css( element, property ) {
		return window.getComputedStyle( element, null ).getPropertyValue( property );
	}

	function inkout(element){
		element.parent().css('position','relative');
		var startTime = new Date().getTime();
		var currentTime = startTime / 1000;
		var font = element.css('font-size') +' '+ element.css('font-family');
		var color = element.css('color');
		var text = element.html();

		var particles = [];
		var hover = false;

		var cw = element.width(),
		    ch = element.height();
		element.html('');
		var canvas = $('<canvas/>').attr({width: cw, height: ch}).css({display: 'inline-block','vertical-align': 'text-bottom'}).appendTo(element),
		    context = canvas.get(0).getContext("2d");

		function drawText(){
			context.clearRect(0,0,cw,ch);
			context.fillStyle = color;
			context.clearRect(0,0,cw,ch);
			context.font = font;
			context.textAlign = "center";
			context.fillText(text,cw/2, ch - (ch/5));
		}

		$(window).resize(function(){
			element.html(text);
			font = element.css('font-size') +' '+ element.css('font-family');
			particles = [];
			cw = element.width(),
				ch = element.height();
			element.html('');
			canvas = $('<canvas/>').attr({width: cw, height: ch}).css({display: 'inline-block','vertical-align': 'top'}).appendTo(element),
				context = canvas.get(0).getContext("2d");
			drawText();
			scramble();
		});
		drawText();

		$(document).click(function(){
			if (hover){
			    hover = false;
			}else {
			    hover = true;
			}
		});

		var particle = function(x,y,visible,color){
			this.color = 'rgba('+color[0]+','+color[1]+','+color[2]+','+color[3] / 255+')';
			this.visible = visible;
			this.realx = x;
			this.realy = y;

			this.toplace = false;

			this.rate = Math.round(Math.random() * 12) - 8;

			this.spin = Math.round(Math.random() * 2);

			this.x = x;
			this.y = y;

			particles.push(this);
		}
		particle.prototype.draw = function(){
			var l = false;
			if (hover){
				this.toplace = true;
				l = true;
			}
			if(l == false){
				this.toplace = false;
			}

			if(this.toplace == false){
				if(this.spin == 1){
					this.x = this.realx + Math.floor(Math.sin(currentTime) * this.rate);
				} else if(this.spin == 0){
					this.y = this.realy + Math.floor(Math.cos(-currentTime) * this.rate);
				} else {
					this.x = this.realx + Math.floor(Math.sin(-currentTime) * this.rate);
					this.y = this.realy + Math.floor(Math.cos(currentTime) * this.rate);
				}
			} else {
				if(this.x < this.realx){
					this.x++;
				} else if(this.x > this.realx){
					this.x--;
				}
				if(this.y < this.realy){
					this.y++;
				} else if(this.y > this.realy){
					this.y--;
				}
			}

			if(this.visible == true || this.toplace == true){
				context.fillStyle = this.color;
				context.fillRect(this.x, this.y, 1,1);
			}
		}

		function scramble(){
			for(var y = 1; y < ch; y+=1){
				for(var x = 0; x < cw; x++){
					if(context.getImageData(x, y, 1, 1).data[3] >= 1){
						if(Math.round(Math.random() * 3) >= 2){
							new particle(x,y,false,context.getImageData(x, y, 1, 1).data);
						} else {
							new particle(x,y,true,context.getImageData(x, y, 1, 1).data);
						}
					}
				}
			}
		}
		scramble();
		var requestframe = window.requestAnimationFrame || window.webkitRequestAnimationFrame || window.mozRequestAnimationFrame || window.msRequestAnimationFrame || window.oRequestAnimationFrame ||
		    function (callback) {
			    window.setTimeout(callback, 1000 / 60);
		    };
		function loop(){
			var now = new Date().getTime();
			currentTime = (now - startTime) / 1000;
			context.clearRect(0,0,cw,ch);
			for(var i = 0; i < particles.length; i++){
				particles[i].draw();
			}

			requestframe(loop);
		}
		loop();
	}
})();