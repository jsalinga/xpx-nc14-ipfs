(function() {

  OCA.XPXHashes = OCA.XPXHashes || {};

  /**
   * @namespace
   */
  OCA.XPXHashes.Util = {

    /**
     * Initialize the XPXHashes plugin.
     *
     * @param {OCA.Files.FileList} fileList file list to be extended
     */
    attach: function(fileList) {

      if (fileList.id === 'trashbin' || fileList.id === 'files.public') {
        return;
      }

      fileList.registerTabView(new OCA.XPXHashes.XPXHashesTabView('xpxhashesTabView', {}));

    }
  };
})();

OC.Plugins.register('OCA.Files.FileList', OCA.XPXHashes.Util);
