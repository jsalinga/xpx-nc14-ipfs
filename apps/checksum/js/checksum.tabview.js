(function() {

  var ChecksumTabView = OCA.Files.DetailTabView.extend({

    id: 'checksumTabView',
    className: 'tab checksumTabView',

    /**
     * get label of tab
     */
    getLabel: function() {

      return t('checksum', 'Blockchain Hashes');

    },

    /**
     * Renders this details view
     *
     * @abstract
     */
    render: function() {
      this._renderSelectList(this.$el);

      this.delegateEvents({
        'change #choose-algorithm': '_onChangeEvent'
      });

    },

    _renderSelectList: function($el) {
      $el.html('<div class="get-checksum">'
        + '<select id="choose-algorithm">'
          + '<option value="">' + t('checksum', 'Hash Type') + '</option>'
          + '<option value="nem">NEM Hash</option>'
          + '<option value="ipfs">IPFS Hash</option>'
        + '</select></div>'
      );
    },

    /**
     * show tab only on files
     */
    canDisplay: function(fileInfo) {

      if(fileInfo != null) {
        if(!fileInfo.isDirectory()) {
          return true;
        }
      }
      return false;

    },

    /**
     * ajax callback for generating md5 hash
     */
    check: function(fileInfo, algorithmType, fileID) {
      // skip call if fileInfo is null
      if(null == fileInfo) {
        _self.updateDisplay({
          response: 'error',
          msg: t('checksum', 'No fileinfo provided.')
        });

        return;
      }

      var url = OC.generateUrl('/apps/checksum/check'),
          data = {source: fileInfo.getFullPath(), type: algorithmType, fileID: fileInfo.id},
          _self = this;
      $.ajax({
        type: 'GET',
        url: url,
        dataType: 'json',
        data: data,
        async: true,
        success: function(data) {
          _self.updateDisplay(data, algorithmType);
        }
      });

    },

    /**
     * display message from ajax callback
     */
    updateDisplay: function(data, algorithmType) {

      var msg = '';
      if('success' == data.response) {
        var array;
        if (algorithmType == 'nem'){
          console.log("NEM:"+algorithmType);
          array = data.msg.split('||');
          msg = algorithmType + ': ' + array[0];
        } else if (algorithmType == 'ipfs'){
          console.log("IPFS:"+algorithmType);
          array = data.msg.split('||');
          msg = algorithmType + ': ' + array[1];
        } else {
          msg = algorithmType + ': ' + data.msg;
        }
      }
      if('error' == data.response) {
        msg = data.msg;
      }

      msg += '<br><br><a id="reload-checksum" class="icon icon-history" style="display:block" href=""></a>';

      this.delegateEvents({
        'click #reload-checksum': '_onReloadEvent'
      });

      this.$el.find('.get-checksum').html(msg);

    },

    /**
     * changeHandler
     */
    _onChangeEvent: function(ev) {
      var algorithmType = $(ev.currentTarget).val();
      if(algorithmType != '') {
        this.$el.html('<div style="text-align:center; word-wrap:break-word;" class="get-checksum"><p><img src="'
          + OC.imagePath('core','loading.gif')
          + '"><br><br></p><p>'
          + t('checksum', 'Getting Blockchain Hashes ...')
          + '</p></div>');
        this.check(this.getFileInfo(), algorithmType);
      }
    },

    _onReloadEvent: function(ev) {
      ev.preventDefault();
      this._renderSelectList(this.$el);
      this.delegateEvents({
        'change #choose-algorithm': '_onChangeEvent'
      });
    }

  });

  OCA.Checksum = OCA.Checksum || {};

  OCA.Checksum.ChecksumTabView = ChecksumTabView;

})();