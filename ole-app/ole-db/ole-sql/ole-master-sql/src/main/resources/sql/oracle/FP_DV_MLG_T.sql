TRUNCATE TABLE FP_DV_MLG_T DROP STORAGE
/
INSERT INTO FP_DV_MLG_T (DV_MLG_EFF_DT,DV_MLG_LMT_AMT,OBJ_ID,VER_NBR,DV_MLG_RT)
  VALUES (TO_DATE( '20030701000000', 'YYYYMMDDHH24MISS' ),0.0,'0D9056D0301839FEE043814FD88139FE',1.0,0.375)
/
INSERT INTO FP_DV_MLG_T (DV_MLG_EFF_DT,DV_MLG_LMT_AMT,OBJ_ID,VER_NBR,DV_MLG_RT)
  VALUES (TO_DATE( '20030701000000', 'YYYYMMDDHH24MISS' ),500.0,'0D9056D0301939FEE043814FD88139FE',1.0,0.18)
/
INSERT INTO FP_DV_MLG_T (DV_MLG_EFF_DT,DV_MLG_LMT_AMT,OBJ_ID,VER_NBR,DV_MLG_RT)
  VALUES (TO_DATE( '20030701000000', 'YYYYMMDDHH24MISS' ),3000.0,'0D9056D0301A39FEE043814FD88139FE',1.0,0.0)
/
