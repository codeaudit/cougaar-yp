# MySQL dump 8.14
#
# Host: localhost    Database: tempcopy
#--------------------------------------------------------
# Server version	3.23.39-nt

#
# Dumping data for table 'alib_component'
#

LOCK TABLES alib_component WRITE;
REPLACE INTO alib_component (COMPONENT_ALIB_ID, COMPONENT_NAME, COMPONENT_LIB_ID, COMPONENT_TYPE, CLONE_SET_ID) VALUES ('YPSupport','YPSupport','recipe|##RECIPE_CLASS##','recipe',0.000000000000000000000000000000);
REPLACE INTO alib_component (COMPONENT_ALIB_ID, COMPONENT_NAME, COMPONENT_LIB_ID, COMPONENT_TYPE, CLONE_SET_ID) VALUES ('YPSupport|YP Support|org.cougaar.yp.YPClientComponent','YPSupport|YP Support|org.cougaar.yp.YPClientComponent','Node.AgentManager.Agent.YPService|org.cougaar.yp.YPClientComponent','Node.AgentManager.Agent.YPService',0.000000000000000000000000000000);
REPLACE INTO alib_component (COMPONENT_ALIB_ID, COMPONENT_NAME, COMPONENT_LIB_ID, COMPONENT_TYPE, CLONE_SET_ID) VALUES ('YPSupport|YP Support|org.cougaar.yp.YPServer','YPSupport|YP Support|org.cougaar.yp.YPServer','plugin|org.cougaar.yp.YPServer','plugin',0.000000000000000000000000000000);
REPLACE INTO alib_component (COMPONENT_ALIB_ID, COMPONENT_NAME, COMPONENT_LIB_ID, COMPONENT_TYPE, CLONE_SET_ID) VALUES ('YPSupport|org.cougaar.servicediscovery.util.yp.PublishTaxonomy','YPSupport|org.cougaar.servicediscovery.util.yp.PublishTaxonomy','plugin|org.cougaar.servicediscovery.util.yp.PublishTaxonomy','plugin',0.000000000000000000000000000000);
UNLOCK TABLES;

#
# Dumping data for table 'asb_assembly'
#

LOCK TABLES asb_assembly WRITE;
INSERT INTO asb_assembly (ASSEMBLY_ID, ASSEMBLY_TYPE, DESCRIPTION) VALUES ('RCP-0001-YPSupport','RCP','YP Support');
UNLOCK TABLES;

#
# Dumping data for table 'asb_component_arg'
#

LOCK TABLES asb_component_arg WRITE;
UNLOCK TABLES;

#
# Dumping data for table 'asb_component_hierarchy'
#

LOCK TABLES asb_component_hierarchy WRITE;
INSERT INTO asb_component_hierarchy (ASSEMBLY_ID, COMPONENT_ALIB_ID, PARENT_COMPONENT_ALIB_ID, PRIORITY, INSERTION_ORDER) VALUES ('RCP-0001-YPSupport','YPSupport|YP Support|org.cougaar.yp.YPClientComponent','YPSupport','COMPONENT',1.000000000000000000000000000000);
INSERT INTO asb_component_hierarchy (ASSEMBLY_ID, COMPONENT_ALIB_ID, PARENT_COMPONENT_ALIB_ID, PRIORITY, INSERTION_ORDER) VALUES ('RCP-0001-YPSupport','YPSupport|YP Support|org.cougaar.yp.YPServer','YPSupport','COMPONENT',0.000000000000000000000000000000);
INSERT INTO asb_component_hierarchy (ASSEMBLY_ID, COMPONENT_ALIB_ID, PARENT_COMPONENT_ALIB_ID, PRIORITY, INSERTION_ORDER) VALUES ('RCP-0001-YPSupport','YPSupport|org.cougaar.servicediscovery.util.yp.PublishTaxonomy','YPSupport','COMPONENT',2.000000000000000000000000000000);
UNLOCK TABLES;


#
# Dumping data for table 'lib_component'
#

LOCK TABLES lib_component WRITE;
REPLACE INTO lib_component (COMPONENT_LIB_ID, COMPONENT_TYPE, COMPONENT_CLASS, INSERTION_POINT, DESCRIPTION) VALUES ('recipe|##RECIPE_CLASS##','recipe','##RECIPE_CLASS##','recipe','Added recipe');
REPLACE INTO lib_component (COMPONENT_LIB_ID, COMPONENT_TYPE, COMPONENT_CLASS, INSERTION_POINT, DESCRIPTION) VALUES ('Node.AgentManager.Agent.YPService|org.cougaar.yp.YPClientComponent','Node.AgentManager.Agent.YPService','org.cougaar.yp.YPClientComponent','Node.AgentManager.Agent.YPService','Added plugin');
REPLACE INTO lib_component (COMPONENT_LIB_ID, COMPONENT_TYPE, COMPONENT_CLASS, INSERTION_POINT, DESCRIPTION) VALUES ('plugin|org.cougaar.yp.YPServer','plugin','org.cougaar.yp.YPServer','Node.AgentManager.Agent.PluginManager.Plugin','Added plugin');
REPLACE INTO lib_component (COMPONENT_LIB_ID, COMPONENT_TYPE, COMPONENT_CLASS, INSERTION_POINT, DESCRIPTION) VALUES ('plugin|org.cougaar.servicediscovery.util.yp.PublishTaxonomy','plugin','org.cougaar.servicediscovery.util.yp.PublishTaxonomy','Node.AgentManager.Agent.PluginManager.Plugin','Added plugin');
UNLOCK TABLES;

#
# Dumping data for table 'lib_mod_recipe'
#

LOCK TABLES lib_mod_recipe WRITE;
REPLACE INTO lib_mod_recipe (MOD_RECIPE_LIB_ID, NAME, JAVA_CLASS, DESCRIPTION) VALUES ('RECIPE-0001YPSupport','YPSupport','org.cougaar.tools.csmart.recipe.ComponentCollectionRecipe','No description available');
UNLOCK TABLES;

#
# Dumping data for table 'lib_mod_recipe_arg'
#

LOCK TABLES lib_mod_recipe_arg WRITE;
REPLACE INTO lib_mod_recipe_arg (MOD_RECIPE_LIB_ID, ARG_NAME, ARG_ORDER, ARG_VALUE) VALUES ('RECIPE-0001YPSupport','$$CP=org.cougaar.servicediscovery.util.yp.PublishTaxonomy-2',3.000000000000000000000000000000,'recipeQueryNCAAgent');
REPLACE INTO lib_mod_recipe_arg (MOD_RECIPE_LIB_ID, ARG_NAME, ARG_ORDER, ARG_VALUE) VALUES ('RECIPE-0001YPSupport','$$CP=org.cougaar.yp.YPClientComponent-1',2.000000000000000000000000000000,'recipeQueryAllAgents');
REPLACE INTO lib_mod_recipe_arg (MOD_RECIPE_LIB_ID, ARG_NAME, ARG_ORDER, ARG_VALUE) VALUES ('RECIPE-0001YPSupport','$$CP=org.cougaar.yp.YPServer-0',4.000000000000000000000000000000,'recipeQueryNCAAgent');
REPLACE INTO lib_mod_recipe_arg (MOD_RECIPE_LIB_ID, ARG_NAME, ARG_ORDER, ARG_VALUE) VALUES ('RECIPE-0001YPSupport','Assembly Id',0.000000000000000000000000000000,'RCP-0001-YPSupport');
REPLACE INTO lib_mod_recipe_arg (MOD_RECIPE_LIB_ID, ARG_NAME, ARG_ORDER, ARG_VALUE) VALUES ('RECIPE-0001YPSupport','Target Component Selection Query',1.000000000000000000000000000000,'recipeQuerySelectNothing');
UNLOCK TABLES;

