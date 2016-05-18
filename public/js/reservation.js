$(function () {

  var beautySelect = function () {
    $('select').selectric({
      responsive: true
    });
  };

  var categoryGallery = function () {
    if ($('.category-gallery')[0]) {

    $('.category-gallery ul').each(function(index, elem) {
      var elemID = 'gallery#' + index;
      elem.id = elemID;
      $(elem).lightSlider({
        gallery: true,
        item: 1,
        slideMargin: 0,
        thumbItem: 7,
        keyPress: true,
        onSliderLoad: function(el) {
          el.lightGallery({
            selector: '#' + elemID + ' .lslide'
          });
        },
        currentPagerPosition:'left'
      });
    });

  }
  };

  var categoryTimepicker = function () {
    $('.timepicker').timepicker({
      timeFormat: 'H:i',
      disableTextInput: true
    });
  };

  var tariffsOpen = function () {
    if ($('.tariffs-btn')[0]) {
      $('.tariffs-btn').click(function (e) {
        $(e.currentTarget).parent().addClass('tariffs-open');
      });
    }
  };

  var globalFilt = {
    from:  from,
    to:    to,
    hotel: {},
    room:  {},
    opt:   []
  };
  window.globalFilt = globalFilt;
  
  var hotelOps = {};
  var catOps = {};
    
    var stringToMinutes = function(time) {
      return moment.duration(time).asMinutes();
    };
    
    var checkTime = function(cat, id) {
      catOps[id] = catOps[id] || {
        noTime:  false,
        addDays: false,
        ltnEci:  false,
        btnLco:  false
      };
      
      var hotel = cat.dataset.hotelid;
      var ltnEci = hotelOps[hotel].eci >= catOps.timeIn;
      var btnLco = hotelOps[hotel].lco <= catOps.timeOut;
      catOps[id].ltnEci = ltnEci;
      catOps[id].btnLco = btnLco;
      
      return (ltnEci || btnLco);
    };
    
    var availableTime = function(e, id) {
      catOps[id] = catOps[id] || {
        noTime:  false,
        addDays: false,
        ltnEci:  false,
        btnLco:  false
      };
      console.log(catOps);
      if(catOps[id].noTime) {
        catOps[id].noTime = false;
        $(e.target).parent().find('.time-no-available').hide();
      }
    };
    
    var additionalDays = function(cat, id, e) {
      console.log('additionalDays');
      catOps[id] = catOps[id] || {
        noTime:  false,
        addDays: false,
        ltnEci:  false,
        btnLco:  false
      };
      
      if(checkTime(cat, id)) {
        
        catOps[id].addDays = true;
        if(catOps[id].ltnEci) { 
          (function() {
            var from = +moment(globalFilt.from, "YYYYMMDD").subtract(1, 'd').format("YYYYMMDD000000");
            globalFilt.from = +from;
            $(e.target).parent().find('.additional-days-in').show();
          })()
        } else { 
          $(e.target).parent().find('.additional-days-in').hide();
          globalFilt.from = +$('#checkIn').val().replace(/\./g,'') + '000000'; 
        }
        if(catOps[id].btnLco) {
          (function() {
            var to = +moment(globalFilt.to, "YYYYMMDD").add(1, 'd').format("YYYYMMDD000000");
            globalFilt.to = to; 
            $(e.target).parent().find('.additional-days-out').show();
          })()
        } else {
          $(e.target).parent().find('.additional-days-out').hide();
          globalFilt.to = +$('#checkOut').val().replace(/\./g,'') + '000000';
        }
      } else {
        catOps[id].addDays = false;
        $(cat).find('.additional-days-in').hide();
        $(cat).find('.additional-days-out').hide();
        globalFilt.from = +($('#checkIn').val().replace(/\./g,'') + '000000');
        globalFilt.to = +($('#checkOut').val().replace(/\./g,'') + '000000');
      }
      
    };

    var elemAndVal = function (container, selector, valueType) {
      console.time('elemAndVal');
      console.log(selector);
      var result = {};
      result.element = container.querySelector(selector);
      
      if (!result.element) {
        result.value = 0;
        console.log(result);
        return result;
      }

      result.value = (function () {
        if (result.element.type === 'checkbox') {
          console.timeEnd('elemAndVal');
          return result.element.checked ? true : false;
        }

        if(result.element.tagName === "SELECT" || result.element.tagName === "INPUT") {
          console.timeEnd('elemAndVal');
          return result.element.value;
        }

        console.timeEnd('elemAndVal');
        return result.element.text;

      })();

      if (valueType === 'number') {
        result.value = +result.value;
      }

      console.timeEnd('elemAndVal');

      return result;

    };

    var collectOpts = function (cat) {
      console.time('collectOpts');

      var options = {

        breakfast:    elemAndVal(cat, '[data-breakfast]'),

        eci:          elemAndVal(cat, '[data-eci]'),

        lco:          elemAndVal(cat, '[data-lco]'),

        guestsCnt:    elemAndVal(cat, '[data-guestscnt]', 'number'),
        
        addGuestsCnt: elemAndVal(cat, '[data-addguests]', 'number'),

        roomCnt:      elemAndVal(cat, '[data-roomcnt]', 'number'),

        hotelId:      (cat).dataset.hotelid,

        catId:        (cat).dataset.catid,

        hash:         +$(cat).find(".tariff")[0].dataset.hash

      };
            
      catOps.timeIn = stringToMinutes(options.eci.value);
      catOps.timeOut = stringToMinutes(options.lco.value);

      globalFilt.hotel = {
        name: elemAndVal($('.filtering')[0], '#hotel').value
      };
      if (globalFilt.hotel.name === '') delete globalFilt.hotel.name;

      globalFilt.hotel = JSON.stringify(globalFilt.hotel);


      globalFilt.room = {
        twin: elemAndVal($('.filtering')[0], '#twin').value,
        guests: elemAndVal($('.filtering')[0], '#peopleQuantity', 'number').value
      };
      if (globalFilt.room.twin === false) delete globalFilt.room.twin;

      globalFilt.room = JSON.stringify(globalFilt.room);

      console.log(JSON.stringify(globalFilt.room));
      globalFilt.opt = [];

      globalFilt.opt = JSON.stringify(globalFilt.opt);

      console.timeEnd('collectOpts');
      
      return options;

    };

    var collectFilters = function (elem) {
      console.time('collectFilters');
      var filters = {
        from: elemAndVal(elem, '#checkIn').value.replace(/\./g,'') + '000000',
        to: elemAndVal(elem, '#checkOut').value.replace(/\./g,'') + '000000',
        hotel: {
          name: elemAndVal(elem, '#hotel').value
        },
        room: {
          twin: elemAndVal(elem, '#twin').value,
          guests: elemAndVal(elem, '#peopleQuantity', 'number').value
        }
      };
      if (filters.hotel.name === '') delete filters.hotel.name;
      if (filters.room.twin === false) delete filters.room.twin;
      
      globalFilt.from = +filters.from;
      globalFilt.to = +filters.to;
      
      console.timeEnd('collectFilters');
      console.log(filters);
      return filters;
    };

    var stringOpts = function (cat) {
      console.time('stringOpts');
      // var filters = collectFilters($('.filtering')[0]);

      var opt = collectOpts(cat);

      var data = JSON.stringify({
        'hotelId':          opt.hotelId,
        'catId':            opt.catId,
        'guestsCnt':        opt.guestsCnt.value,
        'addGuestsCnt':     opt.addGuestsCnt.value,
        'tariffGroupsHash': opt.hash,
        'roomCnt':          opt.roomCnt.value,
        'bkf':              opt.breakfast.value,
        'ci':               opt.eci.value,
        'co':               opt.lco.value
      });
      console.log(opt.eci.value);
      
      console.timeEnd('stringOpts');
      return data;

    };

    // ***************** Setup Options ***************************

    var changeSelect = function (cat, elem, maxCnt) {
      console.time('changeSelect');
      
      if (maxCnt === 0 && $(cat).find(elem).find('option').length !== 0) {
        $(cat).find('option-add-guest').hide();
      }

      if (maxCnt !== 0 && $(cat).find(elem).find('option').length !== maxCnt || 
      maxCnt !== 0 && $(cat).find(elem).find('option').length === 0) {
        (function () {
          var i       = 0,
              $select = $(cat).find(elem),
              value   = $select.val() || maxCnt;
          $select.html('');
          for (;i <= maxCnt; i++) {
            var $option = $('<option>');
            $option.val(i).text(i);
            $select.append($option);
          }

          $select.val(value);
          
          if (maxCnt !== 0 && $(cat).find(elem).find('option').length === 0) {
            $(cat).find('option-add-guest').show();
          }

          console.timeEnd('changeSelect');
        })();
      }

    };

    var setupOpt = function (data, cat) {
      console.time('setupOpt');

      changeSelect(cat, '[name="guest"]', data.ctrl.maxGuestCnt);
      
      changeSelect(cat, '[name="addGuest"]', data.ctrl.maxAddGuesstsCnt);

      changeSelect(cat, '[name="room_count"]', data.ctrl.availableRoomCnt);
      
      for (var key in data.ctrl.prices) {
        $(cat).find('[data-tariff-name=' + key + ']').find('.tariff-price').text(data.ctrl.prices[key] + ' грн');
      }
      
      (data.ctrl.eci) ? $(cat).find('.eci').show() : $(cat).find('.eci').hide();
      (data.ctrl.lco) ? $(cat).find('.lco').show() : $(cat).find('.lco').hide();
      
      window.catOps = catOps;
      console.log(window.catOps);

      console.timeEnd('setupOpt');

    };
    
    var createFiltersReqObj = function (opt) {
      
      var result  =  {
        
        name: {
          op : "EQ",
          v : opt.hotel.name
        },
        categories : {
            elFilter: {
              rooms: {
                elFilter: {
                  guestsCnt: {
                    op: "GTEQ",
                    v: opt.room.guests
                  },
                  twin: {
                    op: "EQ",
                    v: opt.room.twin
                  }
                }
            }
          }
        }
        
      };
      
      if (!opt.hotel.name) delete result.name;
      if (!opt.room.twin) delete result.categories.elFilter.rooms.elFilter.twin;
      
      return result;
      
    };

    // ***************************End of setup options***********************

    var changeCat = function (cat) {
      console.time('changeCat');

      cat = cat || '.category-list';

      $(cat).change(function(e) {
        var cat   = $('.category').has(e.target)[0],
            req   = stringOpts(cat),
            catID = cat.dataset.catid;

        console.log("REQUEST: " + req);
        console.log(globalFilt);

        $.ajax(jsRoutes.controllers.Application.category(+globalFilt.from, +globalFilt.to, req, JSON.stringify(createFiltersReqObj(collectFilters($('.filtering')[0])))))
        .done(function( resp ) {
          console.log('RESPONCE:');
          console.log(resp);
          
          if(resp.type === "basic") {
            
            availableTime(e, catID);
           
            additionalDays(cat, catID, e);
            
            setupOpt(resp, cat);
            
          }
          
          if(resp.type === "tariffsRedraw") {
            alert('Тарифы изменились');
            
            availableTime(e, catID);
            
            additionalDays(cat, catID, e);
            
            $(cat).find('.tariff').remove();
            console.log(resp.ctrl.prices.length);
            for(key in resp.ctrl.prices) {
              $(cat).find(".tariffs").append(resp.html);
              setupOpt(resp, cat);
            }
          }
          
          if(resp.type === "fullRedraw" && !checkTime(cat, catID)) {
            alert('Категория изменилась');
            availableTime(e, catID);
            var category = resp.categoryHtml;
            $(cat).replaceWith(category);
            console.log($(category).attr('data-catid'));
            changeCat('[data-catid=' + $(category).attr('data-catid') + ']');
          }
          
          if(resp.type === "gone" && !checkTime(cat, catID)) {
            alert('Категория удалена');
            availableTime(e);
            $(cat).remove();
          }
          
          if(checkTime(cat, catID) && resp.type === "fullRedraw" || checkTime(cat, catID) && resp.type === "gone") {
            catOps[catID] = catOps[catID] || {
              noTime:  false,
              addDays: false,
              ltnEci:  false,
              btnLco:  false
            };
            catOps[catID].noTime = true;
            $(e.target).parent().find('.time-no-available').show();
          }

           console.timeEnd('changeCat');
        });

      });
    };

    var changeFilter = function (selector) {
      console.time('changeFilter');
      $(selector).change(function(e) {
          var container = e.currentTarget,
                req  = createFiltersReqObj(collectFilters(selector)),
                from = +collectFilters(selector).from(),
                to   = +collectFilters(selector).to();
                
                console.log(req);

            $.ajax(jsRoutes.controllers.Application.filter(from, to, JSON.stringify(req)))
            .done(function( response ) {

              $('.category-list').replaceWith(response);

              categoryGallery();

              categoryTimepicker();

              tariffsOpen();

              beautySelect();

              changeCat();
              console.timeEnd('changeFilter');
            });
      });
    };

//   ************************Starting dynamic****************************

    $.ajax(jsRoutes.controllers.Application.getDummyOffers(from, to))
      .done(function( resp ) {
        $('.filtering').after(resp);
        
        hotelOps = (function() {
          var result = {};
          
          $('.hotel').each(function(index, elem) {
            result[elem.id] = {};
            result[elem.id].checkInTime = elem.dataset.checkintime;
            result[elem.id].checkOutTime = elem.dataset.checkouttime;
            result[elem.id].eci = stringToMinutes(elem.dataset.eci);
            result[elem.id].lco = stringToMinutes(elem.dataset.lco);
          });
          
          return result;
          
        })();
        
        console.log(hotelOps);

        $('#checkIn').val(moment((from).toString().slice(0,-6)).format('YYYY.MM.DD'));
        $('#checkOut').val(moment((to).toString().slice(0,-6)).format('YYYY.MM.DD'));

        beautySelect();

        changeCat('.category-list');

        changeFilter($('.filtering')[0]);


        (function () {

          var min = $('#checkIn').val();
          var max = $('#checkOut').val();
          console.log('checkIn value: ' + min);
          $('#checkOut').datetimepicker({
              format:'YYYY.MM.DD',
              formatDate:'YYYY.MM.DD',
              timepicker:false,
              validateOnBlur: true,
              scrollMonth: false,
              scrollTime: false,
              minDate: min
          });

          $('#checkIn').datetimepicker({
              format:'YYYY.MM.DD',
              formatDate:'YYYY.MM.DD',
              timepicker:false,
              validateOnBlur: true,
              scrollMonth: false,
              scrollTime: false,
              minDate: 0,
              maxDate: max
          });

          categoryGallery();

          categoryTimepicker();

          tariffsOpen();

        })();

    });
});
