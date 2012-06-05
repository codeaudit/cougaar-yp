/*
 * jUDDI - An open source Java implementation of UDDI v2.0
 * http://juddi.org/
 *
 * Copyright (c) 2002, InflexionPoint and contributors
 * All rights reserved.
 */

package org.juddi.datastore.jdbc;

import org.juddi.error.JUDDIException;
import org.juddi.datastore.DataStore;
import org.juddi.util.SearchQualifiers;
import org.juddi.util.UUID;

import org.apache.log4j.Logger;
import org.uddi4j.datatype.*;
import org.uddi4j.datatype.assertion.*;
import org.uddi4j.datatype.business.*;
import org.uddi4j.datatype.binding.*;
import org.uddi4j.datatype.service.*;
import org.uddi4j.datatype.tmodel.*;
import org.uddi4j.response.*;
import org.uddi4j.util.*;

import java.sql.Connection;
import java.util.Vector;

/**
 * @author Steve Viens (steve@inflexionpoint.com)
 * @version 0.6.2
 */
public class JDBCDataStore implements DataStore
{
  // private reference to the jUDDI logger
  private static Logger log = Logger.getLogger(JDBCDataStore.class);

  // private db connection associated with this datastore
  private Connection connection = null;

  // private XA transaxtion object
  private Transaction transaction = null;

  /**
   *
   */
  JDBCDataStore(Connection conn)
  {
    this.connection = conn;
  }

  /**
   *
   */
  Connection getConnection()
  {
    return this.connection;
  }

 /**
  * begin a new transaction
  */
  public void beginTrans()
    throws JUDDIException
  {
    try {
      this.transaction = new Transaction();
      this.transaction.begin(connection);
    }
    catch(java.sql.SQLException sqlex) {
      throw new JUDDIException(sqlex);
    }
  }

 /**
  * commit on all connections.
  */
  public void commit()
    throws JUDDIException
  {
    try {
      this.transaction.commit();
    }
    catch(java.sql.SQLException sqlex) {
      throw new JUDDIException(sqlex);
    }
  }

 /**
  * rollback on all connections.
  */
  public void rollback()
    throws JUDDIException
  {
    try {
      this.transaction.rollback();
    }
    catch(java.sql.SQLException sqlex) {
      throw new JUDDIException(sqlex);
    }
  }

  /**
   *
   */
  public void saveBusiness(BusinessEntity business,String publisherID)
    throws JUDDIException
  {
    try
    {
      if ((business != null) && (connection != null))
      {
        String businessKey = business.getBusinessKey();

        // insert the BusinessEntity object
        BusinessEntityTable.insert(business,publisherID,connection);

        // insert all of the BusinessEntity Name objects
        if (business.getNameVector() != null)
          BusinessNameTable.insert(businessKey,business.getNameVector(),connection);

        // insert all of the BusinessEntity Description objects
        if (business.getDescriptionVector() != null)
          BusinessDescTable.insert(businessKey,business.getDescriptionVector(),connection);

        // insert the BusinessEntity's IdentiferBag KeyedReferences
        IdentifierBag idBag = business.getIdentifierBag();
        if ((idBag != null) && (idBag.getKeyedReferenceVector() != null))
          BusinessIdentifierTable.insert(businessKey,idBag.getKeyedReferenceVector(),connection);

        // insert the BusinessEntity's CategoryBag KeyedReferences
        CategoryBag catBag = business.getCategoryBag();
        if ((catBag != null) && (catBag.getKeyedReferenceVector() != null))
          BusinessCategoryTable.insert(businessKey,catBag.getKeyedReferenceVector(),connection);

        // insert the BusinessEntity's DiscoveryURLs
        DiscoveryURLs discURLs = business.getDiscoveryURLs();
        if ((discURLs != null) && (discURLs.getDiscoveryURLVector() != null))
          DiscoveryURLTable.insert(businessKey,discURLs.getDiscoveryURLVector(),connection);

        // insert the BusinessEntity's Contact objects & information
        Contacts contacts = business.getContacts();
        if (contacts != null)
        {
          Vector contactVector = contacts.getContactVector();
          if ((contactVector != null) && (contactVector.size() > 0))
          {
            // insert the BusinessEntity's Contact objects
            ContactTable.insert(businessKey,contacts.getContactVector(),connection);

            // insert the BusinessEntity's Contact Phone, Address and Email Info
            int listSize = contactVector.size();
            for (int contactID=0; contactID<listSize; contactID++)
            {
              Contact contact = (Contact)contactVector.elementAt(contactID);
              ContactDescTable.insert(businessKey,contactID,contact.getDescriptionVector(),connection);
              EmailTable.insert(businessKey,contactID,contact.getEmailVector(),connection);
              PhoneTable.insert(businessKey,contactID,contact.getPhoneVector(),connection);
              AddressTable.insert(businessKey,contactID,contact.getAddressVector(),connection);

              // insert the Contact's AddressLine objects
              Vector addrList = contact.getAddressVector();
              int addrListSize = addrList.size();
              for (int addrID=0; addrID<addrListSize; addrID++)
              {
                Address address = (Address)addrList.elementAt(addrID);
                AddressLineTable.insert(businessKey,contactID,addrID,address.getAddressLineVector(),connection);
              }
            }
          }
        }

        // 'save' the BusinessEntity's BusinessService objects
        BusinessServices services = business.getBusinessServices();
        if ((services != null) && (services.getBusinessServiceVector() != null))
        {
          Vector serviceVector = services.getBusinessServiceVector();
          int serviceListSize = serviceVector.size();
          for (int j=0; j<serviceListSize; j++)
          {
            BusinessService service = (BusinessService)serviceVector.elementAt(j);
            service.setBusinessKey(businessKey);
            service.setServiceKey(UUID.nextID());
            saveService(service);
          }
        }
      }
    }
    catch(java.sql.SQLException sqlex)
    {
      throw new JUDDIException(sqlex);
    }
  }

  /**
   *
   */
  public BusinessEntity fetchBusiness(String businessKey)
    throws JUDDIException
  {
    BusinessEntity business = null;

    try
    {
      if ((businessKey != null) && (connection != null))
      {
        business = BusinessEntityTable.select(businessKey,connection);
        business.setNameVector(BusinessNameTable.select(businessKey,connection));
        business.setDescriptionVector(BusinessDescTable.select(businessKey,connection));

        IdentifierBag identifierBag = new IdentifierBag();
        identifierBag.setKeyedReferenceVector(BusinessIdentifierTable.select(businessKey,connection));
        business.setIdentifierBag(identifierBag);

        CategoryBag categoryBag = new CategoryBag();
        categoryBag.setKeyedReferenceVector(BusinessCategoryTable.select(businessKey,connection));
        business.setCategoryBag(categoryBag);

        DiscoveryURLs discoveryURLs = new DiscoveryURLs();
        discoveryURLs.setDiscoveryURLVector(DiscoveryURLTable.select(businessKey,connection));
        business.setDiscoveryURLs(discoveryURLs);

        // 'select' the BusinessEntity's Contact objects
        Vector contactList = ContactTable.select(businessKey,connection);
        for (int contactID=0; contactID<contactList.size(); contactID++)
        {
          Contact contact = (Contact)contactList.elementAt(contactID);
          contact.setPhoneVector(PhoneTable.select(businessKey,contactID,connection));
          contact.setEmailVector(EmailTable.select(businessKey,contactID,connection));

          Vector addressList = AddressTable.select(businessKey,contactID,connection);
          for (int addressID=0; addressID<addressList.size(); addressID++)
          {
            Address address = (Address)addressList.elementAt(addressID);
            address.setAddressLineVector(AddressLineTable.select(businessKey,contactID,addressID,connection));
          }
          contact.setAddressVector(addressList);
        }

        Contacts contacts = new Contacts();
        contacts.setContactVector(contactList);
        business.setContacts(contacts);

        // 'fetch' the BusinessEntity's BusinessService objects
        Vector serviceVector = fetchServiceByBusinessKey(businessKey);
        BusinessServices services = new BusinessServices();
        services.setBusinessServiceVector(serviceVector);
        business.setBusinessServices(services);
      }
    }
    catch(java.sql.SQLException sqlex)
    {
      throw new JUDDIException(sqlex);
    }

    return business;
  }

  /**
   *
   */
  public void deleteBusiness(String businessKey)
    throws JUDDIException
  {
    try
    {
      if ((businessKey != null) && (connection != null))
      {
        // delete the BusinessEntity's Services (and dependents)
        deleteServiceByBusinessKey(businessKey);

        // delete the dependents of BusinessEntity
        AddressLineTable.delete(businessKey,connection);
        AddressTable.delete(businessKey,connection);
        EmailTable.delete(businessKey,connection);
        PhoneTable.delete(businessKey,connection);
        ContactDescTable.delete(businessKey,connection);
        ContactTable.delete(businessKey,connection);
        DiscoveryURLTable.delete(businessKey,connection);
        BusinessIdentifierTable.delete(businessKey,connection);
        BusinessCategoryTable.delete(businessKey,connection);
        BusinessDescTable.delete(businessKey,connection);
        BusinessNameTable.delete(businessKey,connection);

        // finally, delete the BusinessEntity itself.
        BusinessEntityTable.delete(businessKey,connection);
      }
    }
    catch(java.sql.SQLException sqlex)
    {
      log.error(sqlex.getMessage(),sqlex);
      throw new JUDDIException(sqlex);
    }
  }

  /**
   *
   */
  public boolean isBusinessPublisher(String businessKey,String publisherID)
    throws JUDDIException
  {
    try
    {
      if ((publisherID != null) && (businessKey != null) && (connection != null))
        return BusinessEntityTable.verifyOwnership(businessKey,publisherID,connection);
    }
    catch(java.sql.SQLException sqlex)
    {
      throw new JUDDIException(sqlex);
    }

    // default to false
    return false;
  }

  /**
   *
   */
  public boolean isValidBusinessKey(String businessKey)
    throws JUDDIException
  {
    try
    {
      if ((businessKey != null) && (connection != null) &&
          (BusinessEntityTable.select(businessKey,connection) != null))
        return true;
    }
    catch(java.sql.SQLException sqlex)
    {
      throw new JUDDIException(sqlex);
    }

    // default to false
    return false;
  }

  /**
   *
   */
  public void saveService(BusinessService service)
    throws JUDDIException
  {
    try
    {
      if ((service != null) && (connection != null))
      {
        String serviceKey = service.getServiceKey();

        // insert the BusinessService object
        BusinessServiceTable.insert(service,connection);

        // insert all of the BusinessService's Name objects
        if (service.getNameVector() != null)
          ServiceNameTable.insert(serviceKey,service.getNameVector(),connection);

        // insert all of the BusinessService's Description objects
        if (service.getDescriptionVector() != null)
          ServiceDescTable.insert(serviceKey,service.getDescriptionVector(),connection);

        // insert the BusinessService's CategoryBag KeyedReferences
        CategoryBag catBag = service.getCategoryBag();
        if ((catBag != null) && (catBag.getKeyedReferenceVector() != null))
          ServiceCategoryTable.insert(serviceKey,catBag.getKeyedReferenceVector(),connection);

        // extract the binding template objects
        BindingTemplates bindings = service.getBindingTemplates();
        if (bindings == null)
          return; // no binding templates were present

        // convert the binding templates to a vector of templates
        Vector bindingList = bindings.getBindingTemplateVector();
        if (bindingList == null)
          return; // a binding template vector wasn't found

        // save all of the binding templates that were found
        int listSize = bindingList.size();
        for (int i=0; i<listSize; i++)
        {
          BindingTemplate binding = (BindingTemplate)bindingList.elementAt(i);
          binding.setServiceKey(serviceKey);
          binding.setBindingKey(UUID.nextID());
          saveBinding(binding);
        }
      }
    }
    catch(java.sql.SQLException sqlex)
    {
      throw new JUDDIException(sqlex);
    }
  }

  /**
   *
   */
  public BusinessService fetchService(String serviceKey)
    throws JUDDIException
  {
    BusinessService service = null;

    try
    {
      if ((serviceKey != null) && (connection != null))
      {
        service = BusinessServiceTable.select(serviceKey,connection);
        service.setNameVector(ServiceNameTable.select(serviceKey,connection));
        service.setDescriptionVector(ServiceDescTable.select(serviceKey,connection));

        CategoryBag bag = new CategoryBag();
        bag.setKeyedReferenceVector(ServiceCategoryTable.select(serviceKey,connection));
        service.setCategoryBag(bag);

        // 'fetch' the BusinessService's BindingTemplate objects
        Vector bindingVector = fetchBindingByServiceKey(serviceKey);
        BindingTemplates bindings = new BindingTemplates();
        bindings.setBindingTemplateVector(bindingVector);
        service.setBindingTemplates(bindings);
      }
    }
    catch(java.sql.SQLException sqlex)
    {
      throw new JUDDIException(sqlex);
    }

    return service;
  }

  /**
   *
   */
  public void deleteService(String serviceKey)
    throws JUDDIException
  {
    try
    {
      if ((serviceKey != null) && (connection != null))
      {
        // delete the BusinessService's BindingTemplates (and dependents)
        deleteBindingByServiceKey(serviceKey);

        // delete the immediate dependents of BusinessService
        ServiceNameTable.delete(serviceKey,connection);
        ServiceDescTable.delete(serviceKey,connection);
        ServiceCategoryTable.delete(serviceKey,connection);

        // finally, delete the BusinessService itself.
        BusinessServiceTable.delete(serviceKey,connection);
      }
    }
    catch(java.sql.SQLException sqlex)
    {
      throw new JUDDIException(sqlex);
    }
  }

  /**
   *
   */
  private Vector fetchServiceByBusinessKey(String businessKey)
    throws JUDDIException
  {
    Vector serviceList = new Vector();

    try
    {
      if ((businessKey != null) && (connection != null))
      {
        Vector tempList = BusinessServiceTable.selectByBusinessKey(businessKey,connection);
        for (int i=0; i<tempList.size(); i++)
        {
          BusinessService service = (BusinessService)tempList.elementAt(i);
          serviceList.add(fetchService(service.getServiceKey()));
        }
      }
    }
    catch(java.sql.SQLException sqlex)
    {
      throw new JUDDIException(sqlex);
    }

    return serviceList;
  }

  /**
   *
   */
  private void deleteServiceByBusinessKey(String businessKey)
    throws JUDDIException
  {
    try
    {
      if ((businessKey != null) && (connection != null))
      {
        // obtain a vector of BusinessServices associated with the BusinessKey
        Vector services = BusinessServiceTable.selectByBusinessKey(businessKey,connection);

        // loop through the vector deleting each service in turn
        int listSize = services.size();
        for (int i=0; i<listSize; i++)
        {
          BusinessService service = (BusinessService)services.elementAt(i);
          deleteService(service.getServiceKey());
        }
      }
    }
    catch(java.sql.SQLException sqlex)
    {
      throw new JUDDIException(sqlex);
    }

  }

  /**
   *
   */
  public boolean isValidServiceKey(String serviceKey)
    throws JUDDIException
  {
    try
    {
      if ((serviceKey != null) && (connection != null) &&
          (BusinessServiceTable.select(serviceKey,connection) != null))
        return true;
    }
    catch(java.sql.SQLException sqlex)
    {
      throw new JUDDIException(sqlex);
    }

    // default to false
    return false;
  }

  /**
   *
   */
  public boolean isServicePublisher(String serviceKey,String publisherID)
    throws JUDDIException
  {
    try
    {
      if ((publisherID != null) && (serviceKey != null) && (connection != null))
        return BusinessServiceTable.verifyOwnership(serviceKey,publisherID,connection);
    }
    catch(java.sql.SQLException sqlex)
    {
      throw new JUDDIException(sqlex);
    }

    // default to false
    return false;
  }

  /**
   *
   */
  public void saveBinding(BindingTemplate binding)
    throws JUDDIException
  {
    try
    {
      if ((binding != null) && (connection != null))
      {
        String bindingKey = binding.getBindingKey();

        // insert the BindingTemplate object
        BindingTemplateTable.insert(binding,connection);

        // insert all of the BindingTemplate's Description objects
        if (binding.getDescriptionVector() != null)
          BindingDescTable.insert(bindingKey,binding.getDescriptionVector(),connection);

        TModelInstanceDetails details = binding.getTModelInstanceDetails();
        if (details == null)
          return;

        Vector detailsVector = details.getTModelInstanceInfoVector();
        if (detailsVector == null)
          return;

        TModelInstanceInfoTable.insert(bindingKey,detailsVector,connection);

        // save all of the BindingTemplate objects
        Vector infoList = details.getTModelInstanceInfoVector();

        int listSize = infoList.size();
        for (int infoID=0; infoID<listSize; infoID++)
        {
          TModelInstanceInfo info = (TModelInstanceInfo)infoList.elementAt(infoID);
          TModelInstanceInfoDescTable.insert(binding.getBindingKey(),infoID,info.getDescriptionVector(),connection);

          InstanceDetails instDetails = info.getInstanceDetails();
          if (instDetails != null)
          {
            InstanceDetailsDescTable.insert(binding.getBindingKey(),infoID,instDetails.getDescriptionVector(),connection);

            OverviewDoc overDoc = instDetails.getOverviewDoc();
            if (overDoc != null)
              InstanceDetailsDocDescTable.insert(binding.getBindingKey(),infoID,overDoc.getDescriptionVector(),connection);
          }
        }
      }
    }
    catch(java.sql.SQLException sqlex)
    {
      throw new JUDDIException(sqlex);
    }
  }

  /**
   *
   */
  public BindingTemplate fetchBinding(String bindingKey)
    throws JUDDIException
  {
    BindingTemplate binding = null;

    try
    {
      if ((bindingKey != null) && (connection != null))
      {
        // fetch the BindingTempate and it's Description Vector
        binding = BindingTemplateTable.select(bindingKey,connection);
        binding.setDescriptionVector(BindingDescTable.select(bindingKey,connection));

        // fetch the BindingTemplate's TModelInstanceInfos
        Vector infoVector = TModelInstanceInfoTable.select(bindingKey,connection);
        if (infoVector != null)
        {
          int vectorSize = infoVector.size();
          for (int infoID=0; infoID<vectorSize; infoID++)
          {
            TModelInstanceInfo info = (TModelInstanceInfo)infoVector.elementAt(infoID);

            // fetch the TModelInstanceInfo Descriptions
            info.setDescriptionVector(TModelInstanceInfoDescTable.select(bindingKey,infoID,connection));

            InstanceDetails instDetails = info.getInstanceDetails();
            if (instDetails != null)
            {
              // fetch the InstanceDetail Descriptions
              instDetails.setDescriptionVector(InstanceDetailsDescTable.select(bindingKey,infoID,connection));

              // fetch the InstanceDetail OverviewDoc Descrptions
              OverviewDoc overDoc = instDetails.getOverviewDoc();
              if (overDoc != null)
              {
                overDoc.setDescriptionVector(InstanceDetailsDocDescTable.select(bindingKey,infoID,connection));
                instDetails.setOverviewDoc(overDoc);
              }
            }
          }

          TModelInstanceDetails details = new TModelInstanceDetails();
          details.setTModelInstanceInfoVector(infoVector);
          binding.setTModelInstanceDetails(details);
        }
      }
    }
    catch(java.sql.SQLException sqlex)
    {
      throw new JUDDIException(sqlex);
    }

    return binding;
  }

  /**
   *
   */
  public void deleteBinding(String bindingKey)
    throws JUDDIException
  {
    try
    {
      if ((bindingKey != null) && (connection != null))
      {
        // delete the immediate dependents of BindingTemplate
        BindingDescTable.delete(bindingKey,connection);
        TModelInstanceInfoDescTable.delete(bindingKey,connection);
        InstanceDetailsDocDescTable.delete(bindingKey,connection);
        InstanceDetailsDescTable.delete(bindingKey,connection);
        TModelInstanceInfoTable.delete(bindingKey,connection);

        // finally, delete the BindingTemplate itself.
        BindingTemplateTable.delete(bindingKey,connection);
      }
    }
    catch(java.sql.SQLException sqlex)
    {
      throw new JUDDIException(sqlex);
    }
  }

  /**
   *
   */
  private Vector fetchBindingByServiceKey(String serviceKey)
    throws JUDDIException
  {
    Vector bindingList = new Vector();

    try
    {
      if ((serviceKey != null) && (connection != null))
      {
        Vector tempList = BindingTemplateTable.selectByServiceKey(serviceKey,connection);
        for (int i=0; i<tempList.size(); i++)
        {
          BindingTemplate binding = (BindingTemplate)tempList.elementAt(i);
          bindingList.add(fetchBinding(binding.getBindingKey()));
        }
      }
    }
    catch(java.sql.SQLException sqlex)
    {
      throw new JUDDIException(sqlex);
    }

    return bindingList;
  }

  /**
   *
   */
  private void deleteBindingByServiceKey(String serviceKey)
    throws JUDDIException
  {
    try
    {
      if ((serviceKey != null) && (connection != null))
      {
        // obtain a vector of BusinessServices associated with the BusinessKey
        Vector bindings = BindingTemplateTable.selectByServiceKey(serviceKey,connection);

        // loop through the vector deleting each service in turn
        int listSize = bindings.size();
        for (int i=0; i<listSize; i++)
        {
          BindingTemplate binding = (BindingTemplate)bindings.elementAt(i);
          deleteBinding(binding.getBindingKey());
        }
      }
    }
    catch(java.sql.SQLException sqlex)
    {
      throw new JUDDIException(sqlex);
    }
  }

  /**
   *
   */
  public boolean isValidBindingKey(String bindingKey)
    throws JUDDIException
  {
    try
    {
      if ((bindingKey != null) && (connection != null) &&
          (BindingTemplateTable.select(bindingKey,connection) != null))
        return true;
    }
    catch(java.sql.SQLException sqlex)
    {
      throw new JUDDIException(sqlex);
    }
    // default to false
    return false;
  }

  /**
   *
   */
  public boolean isBindingPublisher(String bindingKey,String publisherID)
    throws JUDDIException
  {
    try
    {
      if ((publisherID != null) && (bindingKey != null) && (connection != null))
        return BindingTemplateTable.verifyOwnership(bindingKey,publisherID,connection);
    }
    catch(java.sql.SQLException sqlex)
    {
      throw new JUDDIException(sqlex);
    }

    // default to false
    return false;
  }

  /**
   *
   */
  public void saveTModel(TModel tModel,String authorizedUserID)
    throws JUDDIException
  {
    try
    {
      if ((tModel != null) && (connection != null))
      {
        String tModelKey = tModel.getTModelKey();

        // insert the TModel object
        TModelTable.insert(tModel,authorizedUserID,connection);

        // insert all of the TModel Description objects
        if (tModel.getDescriptionVector() != null)
          TModelDescTable.insert(tModelKey,tModel.getDescriptionVector(),connection);

        // insert the TModel's IdentiferBag KeyedReferences
        IdentifierBag idBag = tModel.getIdentifierBag();
        if ((idBag != null) && (idBag.getKeyedReferenceVector() != null))
          TModelIdentifierTable.insert(tModelKey,idBag.getKeyedReferenceVector(),connection);

        // insert the TModel's CategoryBag KeyedReferences
        CategoryBag catBag = tModel.getCategoryBag();
        if ((catBag != null) && (catBag.getKeyedReferenceVector() != null))
          TModelCategoryTable.insert(tModelKey,catBag.getKeyedReferenceVector(),connection);

        // insert the TModel's OverviewDoc & Descriptions
        OverviewDoc overDoc = tModel.getOverviewDoc();
        if ((overDoc != null) && (overDoc.getDescriptionVector() != null))
        {
          // insert the TModel's OverviewDoc Descriptions
          Vector descVector = overDoc.getDescriptionVector();
          if ((descVector != null) && (descVector.size() > 0))
            TModelDocDescTable.insert(tModelKey,descVector,connection);
        }
      }
    }
    catch(java.sql.SQLException sqlex)
    {
      throw new JUDDIException(sqlex);
    }
  }

  /**
   *
   */
  public TModel fetchTModel(String tModelKey)
    throws JUDDIException
  {
    TModel tModel = null;

    try
    {
      if ((tModelKey != null) && (connection != null))
      {
        tModel = TModelTable.select(tModelKey,connection);
        if (tModel != null)
        {
          tModel.setDescriptionVector(TModelDescTable.select(tModelKey,connection));

          // fetch the TModel CategoryBag
          Vector catVector = TModelCategoryTable.select(tModelKey,connection);
          if (catVector != null)
          {
            CategoryBag catBag = new CategoryBag();
            catBag.setKeyedReferenceVector(catVector);
            tModel.setCategoryBag(catBag);
          }

          // fetch the TModel IdentifierBag
          Vector idVector = TModelIdentifierTable.select(tModelKey,connection);
          if (idVector != null)
          {
            IdentifierBag idBag = new IdentifierBag();
            idBag.setKeyedReferenceVector(idVector);
            tModel.setIdentifierBag(idBag);
          }

          // fetch the TModel OverviewDoc & OverviewDoc Descrptions
          OverviewDoc overDoc = tModel.getOverviewDoc();
          if (overDoc != null)
          {
            overDoc.setDescriptionVector(TModelDocDescTable.select(tModelKey,connection));
            tModel.setOverviewDoc(overDoc);
          }
        }
      }
    }
    catch(java.sql.SQLException sqlex)
    {
      throw new JUDDIException(sqlex);
    }

    return tModel;
  }

  /**
   *
   */
  public void deleteTModel(String tModelKey)
    throws JUDDIException
  {
    try
    {
      if ((tModelKey != null) && (connection != null))
      {
        // delete the dependents of TModel
        TModelCategoryTable.delete(tModelKey,connection);
        TModelDescTable.delete(tModelKey,connection);
        TModelDocDescTable.delete(tModelKey,connection);
        TModelIdentifierTable.delete(tModelKey,connection);

        // delete the TModel itself
        TModelTable.delete(tModelKey,connection);
      }
    }
    catch(java.sql.SQLException sqlex)
    {
      throw new JUDDIException(sqlex);
    }
  }

  /**
   *
   */
  public boolean isValidTModelKey(String tModelKey)
    throws JUDDIException
  {
    try
    {
      if ((tModelKey != null) && (connection != null) &&
          (TModelTable.select(tModelKey,connection) != null))
        return true;
    }
    catch(java.sql.SQLException sqlex)
    {
      throw new JUDDIException(sqlex);
    }

    // default to false
    return false;
  }

  /**
   *
   */
  public boolean isTModelPublisher(String tModelKey,String publisherID)
    throws JUDDIException
  {
    try
    {
      if ((publisherID != null) && (tModelKey != null) && (connection != null))
        return TModelTable.verifyOwnership(tModelKey,publisherID,connection);
    }
    catch(java.sql.SQLException sqlex)
    {
      throw new JUDDIException(sqlex);
    }

    // default to false
    return false;
  }

  /**
   *
   */
  public BusinessInfo fetchBusinessInfo(String businessKey)
    throws JUDDIException
  {
    BusinessInfo info = null;

    if ((businessKey != null) && (connection != null))
    {
      try
      {
        info = new BusinessInfo();
        info.setBusinessKey(businessKey);
        info.setNameVector(BusinessNameTable.select(businessKey,connection));
        info.setDescriptionVector(BusinessDescTable.select(businessKey,connection));
        info.setServiceInfos(fetchServiceInfosByBusinessKey(businessKey,false));
      }
      catch(java.sql.SQLException sqlex)
      {
        throw new JUDDIException(sqlex);
      }
    }

    return info;
  }

  /**
   *
   */
  private ServiceInfos fetchServiceInfosByBusinessKey(String businessKey,boolean includeBusinessKey)
    throws JUDDIException
  {
    Vector serviceInfoVector = new Vector();

    if ((businessKey != null) && (connection != null))
    {
      try
      {
        Vector services = BusinessServiceTable.selectByBusinessKey(businessKey,connection);
        for (int i=0; i<services.size(); i++)
        {
          // make a reference to this BusinessServce to
          // easily harvest ServiceInfo data from it.
          BusinessService service = (BusinessService)services.elementAt(i);
          String serviceKey = service.getServiceKey();

          // okay, create a new ServiceInfo
          ServiceInfo info = new ServiceInfo();
          info.setServiceKey(serviceKey);
          if (includeBusinessKey)
            info.setBusinessKey(businessKey);
          info.setNameVector(ServiceNameTable.select(serviceKey,connection));

          // add this ServiceInfo to the ServiceInfo vector
          serviceInfoVector.add(info);
        }
      }
      catch(java.sql.SQLException sqlex)
      {
        throw new JUDDIException(sqlex);
      }
    }

    ServiceInfos serviceInfos = new ServiceInfos();
    serviceInfos.setServiceInfoVector(serviceInfoVector);
    return serviceInfos;
  }

  /**
   *
   */
  public ServiceInfo fetchServiceInfo(String serviceKey)
    throws JUDDIException
  {
    ServiceInfo info = null;

    if ((serviceKey != null) && (connection != null))
    {
      try
      {
        BusinessService service = BusinessServiceTable.select(serviceKey,connection);
        if (service != null)
        {
          info = new ServiceInfo();
          info.setServiceKey(service.getServiceKey());
          info.setBusinessKey(service.getBusinessKey());
          info.setNameVector(ServiceNameTable.select(serviceKey,connection));
        }
      }
      catch(java.sql.SQLException sqlex)
      {
        throw new JUDDIException(sqlex);
      }
    }

    return info;
  }

  /**
   *
   */
  public TModelInfo fetchTModelInfo(String tModelKey)
    throws JUDDIException
  {
    TModelInfo info = null;

    if ((tModelKey != null) && (connection != null))
    {
      try
      {
        TModel tModel = TModelTable.select(tModelKey,connection);
        info = new TModelInfo();
        info.setTModelKey(tModelKey);
        info.setName(tModel.getName());
      }
      catch(java.sql.SQLException sqlex)
      {
        throw new JUDDIException(sqlex);
      }
    }

    return info;
  }

  /**
   *
   */
  public Vector findBusiness( Vector nameVector,
                              DiscoveryURLs discoveryURLs,
                              IdentifierBag identifierBag,
                              CategoryBag categoryBag,
                              TModelBag tModelBag,
                              FindQualifiers findQualifiers)
    throws JUDDIException
  {
    // wrap the FindQualifiers within a jUDDI SearchQualifiers
    // instance for easier access to the qualifier values.
    SearchQualifiers qualifiers = new SearchQualifiers(findQualifiers);

    Vector keyVector = null;

    try
    {
      if ((tModelBag != null) && (tModelBag.size() > 0))
        keyVector = FindBusinessByTModelKeyQuery.select(tModelBag,keyVector,qualifiers,connection);

      if ((discoveryURLs != null) && (discoveryURLs.size() > 0))
        keyVector = FindBusinessByDiscoveryURLQuery.select(discoveryURLs,keyVector,qualifiers,connection);

      if ((categoryBag != null) && (categoryBag.size() > 0))
        keyVector = FindBusinessByCategoryQuery.select(categoryBag,keyVector,qualifiers,connection);

      if ((identifierBag != null) && (identifierBag.size() > 0))
        keyVector = FindBusinessByIdentifierQuery.select(identifierBag,keyVector,qualifiers,connection);

      // always perform this query - even when not searching by Name!!!
      keyVector = FindBusinessByNameQuery.select(nameVector,keyVector,qualifiers,connection);
    }
    catch(java.sql.SQLException sqlex)
    {
      throw new JUDDIException(sqlex);
    }

    return keyVector;
  }

  /**
   *
   */
  public Vector findService(String businessKey,
                            Vector nameVector,
                            CategoryBag categoryBag,
                            TModelBag tModelBag,
                            FindQualifiers findQualifiers)
    throws JUDDIException
  {
    // wrap the FindQualifiers within a jUDDI SearchQualifiers
    // instance for easier access to the qualifier values.
    SearchQualifiers qualifiers = new SearchQualifiers(findQualifiers);

    Vector keyVector = null;

    try
    {
      if ((tModelBag != null) && (tModelBag.size() > 0))
        keyVector = FindServiceByTModelKeyQuery.select(businessKey,tModelBag,keyVector,qualifiers,connection);

      if ((categoryBag != null) && (categoryBag.size() > 0))
        keyVector = FindServiceByCategoryQuery.select(businessKey,categoryBag,keyVector,qualifiers,connection);

      // always perform this query - even when not searching by Name!!!
      keyVector = FindServiceByNameQuery.select(businessKey,nameVector,keyVector,qualifiers,connection);
    }
    catch(java.sql.SQLException sqlex)
    {
      throw new JUDDIException(sqlex);
    }

    return keyVector;
  }

  /**
   *
   */
  public Vector findTModel( String name,
                            CategoryBag categoryBag,
                            IdentifierBag identifierBag,
                            FindQualifiers findQualifiers)
    throws JUDDIException
  {
    // wrap the FindQualifiers within a jUDDI SearchQualifiers
    // instance for easier access to the qualifier values.
    SearchQualifiers qualifiers = new SearchQualifiers(findQualifiers);

    Vector keyVector = null;

    try
    {
      if ((categoryBag != null) && (categoryBag.size() > 0))
        keyVector = FindTModelByCategoryQuery.select(categoryBag,keyVector,qualifiers,connection);

      if ((identifierBag != null) && (identifierBag.size() > 0))
        keyVector = FindTModelByIdentifierQuery.select(identifierBag,keyVector,qualifiers,connection);

      // always perform this query - even when not searching by Name!!!
      keyVector = FindTModelByNameQuery.select(name,keyVector,qualifiers,connection);
    }
    catch(java.sql.SQLException sqlex)
    {
      throw new JUDDIException(sqlex);
    }

    return keyVector;
  }

  /**
   *
   */
  public Vector findBinding(String serviceKey,
                            TModelBag tModelBag,
                            FindQualifiers findQualifiers)
     throws JUDDIException
  {
    // wrap the FindQualifiers within a jUDDI SearchQualifiers
    // instance for easier access to the qualifier values.
    SearchQualifiers qualifiers = new SearchQualifiers(findQualifiers);

    Vector keyVector = null;

    try
    {
      if ((tModelBag != null) && (tModelBag.size() > 0))
        keyVector = FindBindingByTModelKeyQuery.select(serviceKey,tModelBag,keyVector,qualifiers,connection);
    }
    catch(java.sql.SQLException sqlex)
    {
      throw new JUDDIException(sqlex);
    }

    return keyVector;
  }

  /**
   *
   *  1. Retrieve a Vector of BusinessKeys for related BusinessEntities by calling
   *     FindRelatedBusinessQuery.select(businessKey) or if a KeyedReference was
   *     specified in the query call FindRelatedBusinessQuery.selectWithKeyedRef(businessKey,KeyedReference)
   *
   *  2. Call FindBusinessByNameQuery.select(null,keyVector,qualifiers,connection)
   *     to return the Vector of relatedBusinessKeys from step 1 in the requested
   *     order as specified by any FindQualifiers supplied with the UDDI Request.
   *
   *  3. For each relatedBusinessKey returned in step 2 perform the following:
   *
   *  4. Create a new RelatedBusinessInfo instance.
   *
   *  5. Fetch the related businesses BusinessInfo instance by calling
   *     DataStore.fetchBusinessInfo(relatedBusinessKey) and save the Name
   *     Vector and Description Vector to the RelatedBusinessInfo created in
   *     step 4.
   *
   *  6. Using the businessKey passed in to the query and the relatedBusinessKey
   *     from the BusinessInfo fetched (step 4) get all KeyedReference instances
   *     for this pair of keys and add them to the RelatedBusinessInfo created
   *     in step 4. (only grab KeyedReference values from rows where both
   *     FROM_CHECK and TO_CHECK columns are = 'true'. Do this by making a call to
   *     PublisherAssertionTable.selectBusinessRelationships(businessKey,relatedKey)
   */
  public Vector findRelatedBusinesses(String businessKey,KeyedReference keyedRef,FindQualifiers findQualifiers)
    throws JUDDIException
  {
    // wrap the FindQualifiers within a jUDDI SearchQualifiers
    // instance for easier access to the qualifier values.
    SearchQualifiers qualifiers = new SearchQualifiers(findQualifiers);

    Vector keyVector = null;

    try
    {
      // grab the keys of all businesses related to businessKey.
      if (keyedRef == null)
        keyVector = FindRelatedBusinessQuery.select(businessKey,connection);
      else
        keyVector = FindRelatedBusinessQuery.selectWithKeyedRef(businessKey,keyedRef,connection);

      // returns the keys found above in the approriate order
      keyVector = FindBusinessByNameQuery.select(null,keyVector,qualifiers,connection);
    }
    catch(java.sql.SQLException sqlex)
    {
      throw new JUDDIException(sqlex);
    }

    return keyVector;
  }

  /**
   *
   */
  public Vector findRegisteredBusinesses(String publisherID)
    throws JUDDIException
  {
    Vector keyVector = null;

    try
    {
      // grab the keys of all BusinessEntities published by 'publisherID'.
      keyVector = BusinessEntityTable.selectByPublisherID(publisherID,connection);
    }
    catch(java.sql.SQLException sqlex)
    {
      throw new JUDDIException(sqlex);
    }

    return keyVector;
  }

  /**
   *
   */
  public Vector findRegisteredTModels(String publisherID)
    throws JUDDIException
  {
    Vector keyVector = null;

    try
    {
      // grab the keys of all TModels published by 'publisherID'.
      keyVector = TModelTable.selectByPublisherID(publisherID,connection);
    }
    catch(java.sql.SQLException sqlex)
    {
      throw new JUDDIException(sqlex);
    }

    return keyVector;
  }

  /**
   *
   * For each PublisherAssertion in the Vector of PublisherAssertions passed
   * in perform the following steps:
   *
   *  1. Determine if the BusinessEntity specified in the 'fromKey' is
   *     managed by publisherID retrieved in step 1. Do this by calling:
   *     BusinessEntityTable.selectPublisherID(fromKey);
   *
   *  2. Determine if the BusinessEntity specified in the 'toKey' is
   *     managed by publisherID retrieved in step 1. Do this by calling:
   *     BusinessEntityTable.selectPublisherID(toKey);
   *
   *  3. If at least one of the two BusinessKeys specified in this
   *     PublisherAssertion is managed by providerID then check to see if
   *     a row already exists in the PUBLISHER_ASSERTION table by calling:
   *     PublisherAssertionTable.select(PublisherAssertion assertionIn)
   *
   *  4. If a row doesn't exist then insert a new one (set the values of the
   *     to_check and from_check columns appropriately based on info returned
   *     in steps 3 & 4.) Insert the row by calling:
   *     PublisherAssertionTable.insert(PublisherAssertion,fromCheck,toCheck)
   *
   *  5. If a row does exist and publisherID is responsible for the
   *     BusinessEntity identified by 'fromKey' then update the row by calling:
   *     PublisherAssertionTable.updateFromCheck(PublisherAssertion,true)
   *
   *  6. If a row does exist and publisherID is responsible for the
   *     BusinessEntity identified by 'toKey' then update the row by calling:
   *     PublisherAssertionTable.updateToCheck(PublisherAssertion,true)
   */
  public void saveAssertions(String publisherID,Vector assertions)
    throws JUDDIException
  {
    try
    {
      // iterate through the PublisherAssertion Vector
      for (int i=0; i<assertions.size(); i++)
      {
        // grab the next PublisherAssertion and it's 'fromKey' & 'toKey' values
        PublisherAssertion assertion = (PublisherAssertion)assertions.elementAt(i);
        String fromKey = assertion.getFromKeyString();
        String toKey = assertion.getToKeyString();

        // determine if this assertion's 'fromKey' and/or 'toKey' values are
        // managed by the PublisherID specified.
        boolean fromCheck = BusinessEntityTable.verifyOwnership(fromKey,publisherID,connection);
        boolean toCheck = BusinessEntityTable.verifyOwnership(toKey,publisherID,connection);

        // if a row in the PUBLISHER_ASSERTION table doesn't yet exist then
        // insert one. If a row does already exist then simply update the appropriate
        // 'fromKey' and 'toKey' values.
        if (PublisherAssertionTable.select(assertion,connection) == null)
          PublisherAssertionTable.insert(assertion,fromCheck,toCheck,connection);
        else
        {
          if (fromCheck)
            PublisherAssertionTable.updateFromCheck(assertion,fromCheck,connection);
          if (toCheck)
            PublisherAssertionTable.updateToCheck(assertion,toCheck,connection);
        }
      }
    }
    catch(java.sql.SQLException sqlex)
    {
      throw new JUDDIException(sqlex);
    }
  }

  /**
   *
   * For each PublisherAssertion in the Vector of PublisherAssertions passed
   * in perform the following steps:
   *
   *  1. Determine if the BusinessEntity specified in the 'fromKey' is
   *     managed by publisherID retrieved in step 1. Do this by calling:
   *     BusinessEntityTable.selectPublisherID(fromKey);
   *
   *  2. If the publisherID does manage the BusinessEntity identified by
   *     the PublisherAssertions 'fromKey' then call the following method:
   *     PublisherAssertionTable.updateFromCheck(PublisherAssertion,false)
   *
   *  3. Determine if the BusinessEntity specified in the 'toKey' is
   *     managed by publisherID retrieved in step 1. Do this by calling:
   *     BusinessEntityTable.selectPublisherID(toKey);
   *
   *  4. If the publisherID does manage the BusinessEntity identified by
   *     the PublisherAssertions 'toKey then call the following method:
   *     PublisherAssertionTable.updateToCheck(PublisherAssertion,false)
   *
   *  5. After iterating through the entire Vector of PublisherAssertions
   *    call the following method: PublisherAssertionTable.deleteDeadAssertions()
   */
  public void deleteAssertions(String publisherID,Vector assertions)
    throws JUDDIException
  {
    try
    {
      // iterate through the PublisherAssertion Vector
      for (int i=0; i<assertions.size(); i++)
      {
        // grab a reference to the next PublisherAssertion
        PublisherAssertion assertion = (PublisherAssertion)assertions.elementAt(i);

        // if the PublisherID is equal to the PublisherID of the BusinessEntity
        // specified by the 'fromKey' then set the FROM_CHECK column to 'false'
        String fromID = BusinessEntityTable.selectPublisherID(assertion.getFromKeyString(),connection);
        if (publisherID.equalsIgnoreCase(fromID))
          PublisherAssertionTable.updateFromCheck(assertion,false,connection);

        // if the PublisherID is equal to the PublisherID of the BusinessEntity
        // specified by the 'toKey' then set the TO_CHECK column to 'false'
        String toID = BusinessEntityTable.selectPublisherID(assertion.getToKeyString(),connection);
        if (publisherID.equalsIgnoreCase(toID))
          PublisherAssertionTable.updateToCheck(assertion,false,connection);
      }

      // remove any invalidated rows from the PUBLISHER_ASSERTION table. An
      // invalidated row is any row with a value of 'false' in both the
      // FROM_KEY and TO_KEY columns.
      if (assertions.size() > 0)
        PublisherAssertionTable.deleteDeadAssertions(connection);
    }
    catch(java.sql.SQLException sqlex)
    {
      throw new JUDDIException(sqlex);
    }
  }

  /**
   *
   *  1. Retrieve all BusinessKey's that the publisherID is responsible for
   *     managing by calling: BusinessEntityTable.selectByPublisherID(publisherID)
   *
   *  2. Retrieve all PublisherAssertion's that publisherID has made by
   *     calling: PublisherAssertionTable.selectAssertions(Vector) where
   *     'Vector' is the collection of BusinessKey's returned in step 2.
   */
  public Vector getAssertions(String publisherID)
    throws JUDDIException
  {
    Vector assertions = null;

    try
    {
      Vector keys = BusinessEntityTable.selectByPublisherID(publisherID,connection);
      assertions = PublisherAssertionTable.selectAssertions(keys,connection);
    }
    catch(java.sql.SQLException sqlex)
    {
      throw new JUDDIException(sqlex);
    }

    return assertions;
  }

  /**
   *
   *  1. Retrieve all PublisherAssertions associated with the publisherID
   *     passed in by calling DataSource.getAssertions(publisherID)
   *
   *  2. With the Vector of PublisherAssertions retrieved in step 1 call
   *     DataSource.deleteAssertions(publisherID,Vector)
   *
   *  3. With the Vector of PublisherAssertions passed into this method
   *     call: DataSource.addAssertions(publisherID,Vector)
   */
  public Vector setAssertions(String publisherID,Vector newAssertions)
    throws JUDDIException
  {
    // grab all existing PublisherAssertions with this publisherID
    Vector oldAssertions = getAssertions(publisherID);

    // delete all existing PublisherAsssertions with this publisherID
    deleteAssertions(publisherID,oldAssertions);

    // save all of the new PublisherAssertions
    saveAssertions(publisherID,newAssertions);

    return newAssertions;
  }

  /**
   *
   *  1. Retrieve Vector of BusinessKeys for BusinessEntities managed by
   *     publisherID by calling: BusinessEntityTable.selectByPublisherID(publisherID)
   *
   *  2. Call PublisherAssertionTable.selectBothKeysOwnedAssertions(Vector of BusinessKeys)
   *
   *  3. Call PublisherAssertionTable.selectFromKeyOwnedAssertions(Vector of BusinessKeys)
   *
   *  4. Call PublisherAssertionTable.selectToKeyOwnedAssertions(Vector of BusinessKeys)
   *
   *  5. Combine Vectors from steps 3, 4 and 5 above into one Vector of
   *     AssertionStatusItem instances
   *
   *  5. Loop through Vector from step 6 copying only AssertionStatusItem instances
   *     that have a CompletionStatus that matches the completionStatus requested.
   */
  public Vector getAssertionStatusItems(String publisherID,String completionStatus)
    throws JUDDIException
  {
    Vector items = null;

    try
    {
      // grab a Vector of BusinessKeys managed by PublisherID
      Vector keys = BusinessEntityTable.selectByPublisherID(publisherID,connection);

      // grab any PublisherAssertion (as an AssertionStatusItem) that includes
      // any of the BusinessKeys returned above in the TO_KEY or FROM_KEY field.
      Vector allItems = new Vector();
      allItems.addAll(PublisherAssertionTable.selectBothKeysOwnedAssertion(keys,connection));
      allItems.addAll(PublisherAssertionTable.selectFromKeyOwnedAssertion(keys,connection));
      allItems.addAll(PublisherAssertionTable.selectToKeyOwnedAssertion(keys,connection));

      // if no completionStatua was passed in then simply return all
      // AssertionStatusItems. Otherwise only return the ones that have
      // the 'completionStatus' specified.
      if ((completionStatus == null) || (completionStatus.length() == 0))
        items = allItems;
      else
      {
        // create the Vector to return
        if (allItems.size() > 0)
          items = new Vector();

        // evaluate every AssertionStatusItem
        for (int i=0; i<allItems.size(); i++)
        {
          AssertionStatusItem item = (AssertionStatusItem)allItems.elementAt(i);
          CompletionStatus status = item.getCompletionStatus();
          if (status.getText().equalsIgnoreCase(completionStatus))
            items.addElement(item);
        }
      }
    }
    catch(java.sql.SQLException sqlex)
    {
      throw new JUDDIException(sqlex);
    }

    return items;
  }
}