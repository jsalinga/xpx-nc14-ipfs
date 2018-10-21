<?php
namespace OCA\Checksum\Controller;

use OCP\AppFramework\Controller;
use OCP\IRequest;
use OC\Files\Filesystem;
use OCP\AppFramework\Http\JSONResponse;
//use OCP\DB\QueryBuilder\IQueryBuilder;
use OCP\IDbConnection;

class ChecksumController extends Controller {

		protected $language;

		public function __construct($appName, IRequest $request, IDBConnection $db) {

				parent::__construct($appName, $request);

				// get i10n
				$this->language = \OC::$server->getL10N('checksum');
				$this->db = $db;

		}

		
		/**
		 * callback function to get md5 hash of a file
		 * @NoAdminRequired
		 * @param (string) $source - filename
		 * @param (string) $type - hash algorithm type
		 * @param (integer) $fileID - File ID
		 */ 
	  public function check($source, $type, $fileID) {
	  		if(!$this->checkAlgorithmType($type)) {
	  			return new JSONResponse(
							array(
									'response' => 'error',
									'msg' => $this->language->t('The algorithm type "%s" is not a valid or supported algorithm type.', array($type))
							)
					);
	  		}

				if($hash = $this->getHash($source, $type, $fileID)){
						return new JSONResponse(
								array(
										'response' => 'success',
										'msg' => $hash
								)
						);
				} else {
						return new JSONResponse(
								array(
										'response' => 'error',
										'msg' => $this->language->t('File not found.')
								)
						);
				};

	  }

	  protected function getHash($source, $type, $fileID) {
		if ($type = 'nem' or $type = 'ipfs'){
			$nem_hash = '';
			$SQL = 'SELECT path_hash, etag FROM *PREFIX*filecache WHERE fileid = ? ORDER BY fileid DESC limit 1';
			$stmt = $this->db->prepare($SQL);
			$stmt->bindParam(1, $fileID, \PDO::PARAM_INT);
			$stmt->execute();
			$row = $stmt->fetch();
			$nem_hash = $row['path_hash'] . '||' . $row['etag'];
			$stmt->closeCursor();
			return $nem_hash;			
		} elseif($info = Filesystem::getLocalFile($source)) {
	  		return hash_file($type, $info);
	  	}
	  	return false;
	  }

	  protected function checkAlgorithmType($type) {
	  	$list_algos = hash_algos();
	  	return in_array($type, $this->getAllowedAlgorithmTypes());// && in_array($type, $list_algos);
	  }

	  protected function getAllowedAlgorithmTypes() {
	  	return array('nem', 'ipfs');
		}
}

