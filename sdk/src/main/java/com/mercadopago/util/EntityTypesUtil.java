package com.mercadopago.util;

import android.content.Context;

import com.mercadopago.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by marlanti on 3/21/17.
 */

public class EntityTypesUtil {

    private static final String DEFAULT_SITE = "DEFAULT";
    private static Map<String,Integer> entityTypesResourceids;

    public static List<String> getEntityTypesBySite(String siteId, Context context) {

        int resourceId = getResourceId(siteId);

        List<String> entityTypes = new ArrayList<>();
        Collections.addAll(entityTypes, context.getResources().getStringArray(resourceId));

        return entityTypes;
    }

    private static int getResourceId(String siteId){

        loadEntityTypesResourceIds();

        Integer resourceId = entityTypesResourceids.get(siteId);

        if(resourceId==null){
            resourceId = entityTypesResourceids.get(DEFAULT_SITE);
        }

        return resourceId;
    }

    private static void loadEntityTypesResourceIds(){

        if (entityTypesResourceids==null || entityTypesResourceids.isEmpty()){
            entityTypesResourceids = new HashMap<>();
            entityTypesResourceids.put(SitesUtil.MCO, R.array.entity_types_mco_array);
            entityTypesResourceids.put(SitesUtil.MLB, R.array.entity_types_mlb_array);
            entityTypesResourceids.put(DEFAULT_SITE, R.array.entity_types_default_array);
        }
    }

}
