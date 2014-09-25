/*
-- Query: SELECT * FROM ole.ole_locn_level_t
LIMIT 0, 1000

-- Date: 2014-09-23 13:46
*/
INSERT INTO `ole_locn_level_t` (`LEVEL_ID`,`OBJ_ID`,`VER_NBR`,`LEVEL_CD`,`LEVEL_NAME`,`PARENT_LEVEL`) VALUES ('1','de741fdc-eead-4548-85fe-ed72ff70cd82',1,'INSTITUTION','Institution',NULL);
INSERT INTO `ole_locn_level_t` (`LEVEL_ID`,`OBJ_ID`,`VER_NBR`,`LEVEL_CD`,`LEVEL_NAME`,`PARENT_LEVEL`) VALUES ('2','ea3d499d-cd83-475d-8da0-a6e63df914a5',1,'CAMPUS','Campus','1');
INSERT INTO `ole_locn_level_t` (`LEVEL_ID`,`OBJ_ID`,`VER_NBR`,`LEVEL_CD`,`LEVEL_NAME`,`PARENT_LEVEL`) VALUES ('3','3c20a23b-580a-4279-93a5-175d7bddbd8d',1,'LIBRARY','Library','2');
INSERT INTO `ole_locn_level_t` (`LEVEL_ID`,`OBJ_ID`,`VER_NBR`,`LEVEL_CD`,`LEVEL_NAME`,`PARENT_LEVEL`) VALUES ('4','0c24fffc-51cf-4b98-aafe-d498a8f87150',1,'COLLECTION','Collection','3');
INSERT INTO `ole_locn_level_t` (`LEVEL_ID`,`OBJ_ID`,`VER_NBR`,`LEVEL_CD`,`LEVEL_NAME`,`PARENT_LEVEL`) VALUES ('5','2d20901e-162a-4d1c-9463-bd33e1f8dd57',1,'SHELVING','Shelving Location','4');
