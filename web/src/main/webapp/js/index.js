$(function () {
  $("[data-toggle='tooltip']").tooltip();
});

function viewImg(img) {
  var _img = $(img);
  var viewImg = $('.img-view img');
  viewImg.attr("src", _img.attr('src'));
  $(".img-view a").attr("href", _img.attr('src'));
  var theImage = new Image();
  theImage.src = _img.attr("src");
  var width = theImage.width;
  var height = theImage.height;


  var top = undefined;
  if (height > 700) {
    top = 0
  } else {
    top = 700 / 2 - height / 2
  }
  if (top == 0) {
    var newHeight = height / width * 1200;
    top = 700 / 2 - height / newHeight
  } else {
    top = (700 / 2 - height / 2) * 2
  }

  viewImg.css({
    "margin-top": top / 2 + 'px'
  })
  $(".img-view").show();
}

function closeImgView() {
  $(".img-view").hide();
}