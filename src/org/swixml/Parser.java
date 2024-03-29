/*--
 $Id: Parser.java,v 1.4 2005/06/04 22:17:02 wolfpaulus Exp $

 Copyright (C) 2003-2004 Wolf Paulus.
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions
 are met:

 1. Redistributions of source code must retain the above copyright
 notice, this list of conditions, and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright
 notice, this list of conditions, and the disclaimer that follows
 these conditions in the documentation and/or other materials provided
 with the distribution.

 3. The end-user documentation included with the redistribution,
 if any, must include the following acknowledgment:
        "This product includes software developed by the
         SWIXML Project (http://www.swixml.org/)."
 Alternately, this acknowledgment may appear in the software itself,
 if and wherever such third-party acknowledgments normally appear.

 4. The name "Swixml" must not be used to endorse or promote products
 derived from this software without prior written permission. For
 written permission, please contact <info_AT_swixml_DOT_org>

 5. Products derived from this software may not be called "Swixml",
 nor may "Swixml" appear in their name, without prior written
 permission from the Swixml Project Management.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED.  IN NO EVENT SHALL THE SWIXML PROJECT OR ITS
 CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.
 ====================================================================

 This software consists of voluntary contributions made by many
 individuals on behalf of the Swixml Project and was originally
 created by Wolf Paulus <wolf_AT_swixml_DOT_org>. For more information
 on the Swixml Project, please see <http://www.swixml.org/>.
*/

package org.swixml;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.swixml.converters.ConstraintsConverter;
import org.swixml.converters.LocaleConverter;
import org.swixml.converters.PrimitiveConverter;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.List;

/**
 * Singleton Parser to render XML for Swing Documents
 * <p/>
 * <img src="doc-files/swixml_1_0.png" ALIGN="center">
 * </p>
 *
 * @author <a href="mailto:wolf@paulus.com">Wolf Paulus</a>
 * @author <a href="mailto:fm@authentidate.de">Frank Meissner</a>
 * @version $Revision: 1.4 $
 * @see org.swixml.SwingTagLibrary
 * @see org.swixml.ConverterLibrary
 */
public class Parser {

  //
  //  Custom Attributes
  //

  /**
   * Additional attribute to collect layout constrain information
   */
  public static final String ATTR_CONSTRAINTS = "constraints";

  /**
   * Additional attribute to collect information about the PLAF implementation
   */
  public static final String ATTR_PLAF = "plaf";

  /**
   * Additional attribute to collect layout constrain information
   */
  public static final String ATTR_BUNDLE = "bundle";

  /**
   * Additional attribute to collect layout constrain information
   */
  public static final String ATTR_LOCALE = "locale";

  /**
   * Allows to provides swixml tags with an unique id
   */
  public static final String ATTR_ID = "id";

  /**
   * Allows to provides swixml tags with an unique id
   */
  public static final String ATTR_REFID = "refid";

  /**
   * Allows to provides swixml tags with an unique id
   *
   * @see #ATTR_REFID
   * @deprecated use refid instead
   */
  public static final String ATTR_USE = "use";

  /**
   * Allows to provides swixml tags with an unique id
   */
  public static final String ATTR_INCLUDE = "include";

  /**
   * Allows to provides swixml tags with a dynamic update class
   */
  public static final String ATTR_INITCLASS = "initclass";

  /**
   * Allows to provides swixml tags with a dynamic update class
   */
  public static final String ATTR_ACTION = "action";

  /**
   * Prefix for all MAC OS X related attributes
   */
  public static final String ATTR_MACOS_PREFIX = "macos_";

  /**
   * Attribute name that flags an Action as the default Preferences handler on a Mac
   */
  public static final String ATTR_MACOS_PREF = ATTR_MACOS_PREFIX + "preferences";

  /**
   * Attribute name that flags an Action as the default Aboutbox handler on a Mac
   */
  public static final String ATTR_MACOS_ABOUT = ATTR_MACOS_PREFIX + "about";

  /**
   * Attribute name that flags an Action as the default Quit handler on a Mac
   */
  public static final String ATTR_MACOS_QUIT = ATTR_MACOS_PREFIX + "quit";

  /**
   * Attribute name that flags an Action as the default Open Application handler on a Mac
   */
  public static final String ATTR_MACOS_OPENAPP = ATTR_MACOS_PREFIX + "openapp";

  /**
   * Attribute name that flags an Action as the default Open File handler on a Mac
   */
  public static final String ATTR_MACOS_OPENFILE = ATTR_MACOS_PREFIX + "openfile";

  /**
   * Attribute name that flags an Action as the default Print handler on a Mac
   */
  public static final String ATTR_MACOS_PRINT = ATTR_MACOS_PREFIX + "print";

  /**
   * Attribute name that flags an Action as the default Re-Open Applicaiton handler on a Mac
   */
  public static final String ATTR_MACOS_REOPEN = ATTR_MACOS_PREFIX + "reopen";


  /**
   * Method name used with initclass - if this exit, the update class will no be instanced but getInstance is called
   */
  public static final String GETINSTANCE = "getInstance";

  /**
   * Localiced Attributes
   */
  public static final Vector LOCALIZED_ATTRIBUTES = new Vector();

  //
  //  Private Members
  //

  /**
   * the calling engine
   */
  private SwingEngine engine;

  /**
   * ConverterLib, to access COnverters, converting String in all kinds of things
   */
  private ConverterLibrary cvtlib = ConverterLibrary.getInstance();

  /**
   * map to store id-id components, needed to support labelFor attributes
   */
  private Map lbl_map = new HashMap();

  /**
   * map to store specific Mac OS actions mapping
   */
  private Map mac_map = new HashMap();

  /**
   * docoument, to be parsed
   */
  private Document jdoc;

  /**
   *  Static Initializer adds Attribute Names into the LOCALIZED_ATTRIBUTES Vector
   *  Needs to be inserted all lowercase.
   */
  static {
    LOCALIZED_ATTRIBUTES.add("accelerator");
    LOCALIZED_ATTRIBUTES.add("icon");
    LOCALIZED_ATTRIBUTES.add("iconimage");
    LOCALIZED_ATTRIBUTES.add("label");
    LOCALIZED_ATTRIBUTES.add("mnemonic");
    LOCALIZED_ATTRIBUTES.add("name");
    LOCALIZED_ATTRIBUTES.add("text");
    LOCALIZED_ATTRIBUTES.add("title");
    LOCALIZED_ATTRIBUTES.add("titleat");
    LOCALIZED_ATTRIBUTES.add("titles");
    LOCALIZED_ATTRIBUTES.add("tooltiptext");
  }

  /**
   * Constructs a new SwixMl Parser for the provided engine.
   *
   * @param engine <code>SwingEngine</code>
   */
  public Parser(SwingEngine engine) {
    this.engine = engine;
  }

  /**
   * Converts XML into a javax.swing object tree.
   * <pre>
   * Note: This parse method does not return a swing object but converts all <b>sub</b> nodes
   * of the xml documents root into seing objects and adds those into the provided container.
   * This is useful when a JApplet for instance already exists and need to get some gui inserted.
   * </pre>
   *
   * @param jdoc      <code>Document</code> providing the XML document
   * @param container <code>Container</code> container for the XML root's children
   * @throws Exception
   */
  public void parse(Document jdoc, Container container) throws Exception {
    this.jdoc = jdoc;
    this.lbl_map.clear();
    this.mac_map.clear();
    getSwing(processCustomAttributes(jdoc.getRootElement()), container);

    linkLabels();
    supportMacOS();

    this.lbl_map.clear();
    this.mac_map.clear();
  }

  /**
   * Converts XML into a javax.swing object tree.
   * <pre>
   *    Reads XML from the provied <code>Reader</code> and builds an intermediate jdom document.
   *    Tags and their attributes are getting converted into swing objects.
   * </pre>
   *
   * @param jdoc <code>Document</code> providing the XML document
   * @return <code>java.awt.Container</code> root object for the swing object tree
   * @throws Exception
   */
  public Object parse(Document jdoc) throws Exception {
    this.jdoc = jdoc;
    this.lbl_map.clear();
    Object obj = getSwing(processCustomAttributes(jdoc.getRootElement()), null);

    linkLabels();
    supportMacOS();

    this.lbl_map.clear();
    this.mac_map.clear();
    return obj;
  }

  /**
   * Looks for custom attributes to be proccessed.
   *
   * @param element <code>Element</code> custom attr. tag are looked for in this jdoc element
   * @return <code>Element</code> - passed in (and maybe modified) element
   *         <pre>
   *          <b>Note:</b>
   *          <br>Successfully proccessed custom attributes will be removed from the jdoc element.
   *         </pre>
   */
  private Element processCustomAttributes(Element element) throws Exception {
    //
    //  set Locale
    //
    Attribute locale = element.getAttribute(Parser.ATTR_LOCALE);
    if (locale != null && locale.getValue() != null) {
      engine.setLocale(LocaleConverter.conv(locale));
      element.removeAttribute(Parser.ATTR_LOCALE);
    }
    //
    //  set ResourceBundle
    //
    Attribute bundle = element.getAttribute(Parser.ATTR_BUNDLE);
    if (bundle != null && bundle.getValue() != null) {
      engine.getLocalizer().setResourceBundle(bundle.getValue());
      element.removeAttribute(Parser.ATTR_BUNDLE);
    }
    //
    //  Set Look and Feel based on ATTR_PLAF
    //
    Attribute plaf = element.getAttribute(Parser.ATTR_PLAF);
    if (plaf != null && plaf.getValue() != null && 0 < plaf.getValue().length()) {
      try {
        if (!(SwingEngine.isMacOSXSupported() && SwingEngine.isMacOSX())) {
          UIManager.setLookAndFeel(plaf.getValue());
        }
      } catch (Exception e) {
        if (SwingEngine.DEBUG_MODE) {
          System.err.println(e);
        }
      }
      element.removeAttribute(Parser.ATTR_PLAF);
    }

    return element;
  }

  /**
   * Helper Method to Link Labels to InputFields etc.
   */
  private void linkLabels() {
    Iterator it = lbl_map.keySet().iterator();
    while (it != null && it.hasNext()) {
      JLabel lbl = (JLabel) it.next();
      String id = lbl_map.get(lbl).toString();
      try {
        lbl.setLabelFor((Component) engine.getIdMap().get(id));
      } catch (ClassCastException e) {
        // intent. empty.
      }
    }
  };

  /**
   * Link actions with the MacOS' system menu bar
   */
  private void supportMacOS() {
    if (SwingEngine.isMacOSXSupported() && SwingEngine.isMacOSX()) {
      try {
        MacApp.getInstance().update(mac_map);
      } catch (Throwable t) {
        // intentionally empty
      }
    }
  }

  /**
   * Recursively converts <code>org.jdom.Element</code>s into <code>javax.swing</code> or <code>java.awt</code> objects
   *
   * @param element <code>org.jdom.Element</code> XML tag
   * @param obj     <code>Object</code> if not null, only this elements children will be processed, not the element itself
   * @return <code>java.awt.Container</code> representing the GUI impementation of the XML tag.
   * @throws Exception
   */
  Object getSwing(Element element, Object obj) throws Exception {

    Factory factory = engine.getTaglib().getFactory(element.getName());
    //  look for <id> attribute value
    String id = element.getAttribute(Parser.ATTR_ID) != null ? element.getAttribute(Parser.ATTR_ID).getValue().trim() : null;
    //  either there is no id or the id is not user so far
    boolean unique = !engine.getIdMap().containsKey(id);
    boolean constructed = false;

    if (!unique) {
      throw new IllegalStateException("id already in use: " + id + " : " + engine.getIdMap().get(id).getClass().getName());
    }
    if (factory == null) {
      throw new Exception("Unknown TAG, implementation class not defined: " + element.getName());
    }

    //
    //  XInclude
    //

    if (element.getAttribute(Parser.ATTR_INCLUDE) != null) {
      StringTokenizer st = new StringTokenizer(element.getAttribute(Parser.ATTR_INCLUDE).getValue(), "#");
      element.removeAttribute(Parser.ATTR_INCLUDE);
      Document doc = new org.jdom.input.SAXBuilder().build(this.engine.getClassLoader().getResourceAsStream(st.nextToken()));
      Element xelem = find(doc.getRootElement(), st.nextToken());
      if (xelem != null) {
        moveContent(xelem, element);
      }
    }

    //
    //  clone attribute if <em>use</em> attribute is available
    //


    if (element.getAttribute(Parser.ATTR_REFID) != null) {
      element = (Element) element.clone();
      cloneAttributes(element);
      element.removeAttribute(Parser.ATTR_REFID);
    } else if (element.getAttribute(Parser.ATTR_USE) != null) {
      element = (Element) element.clone();
      cloneAttributes(element);
      element.removeAttribute(Parser.ATTR_USE);
    }
    //
    //  let the factory instance a new object
    //

    List attributes = element.getAttributes();
    if (obj == null) {
      Object initParameter = null;

      if (element.getAttribute(Parser.ATTR_INITCLASS) != null) {
        StringTokenizer st = new StringTokenizer(element.getAttributeValue(Parser.ATTR_INITCLASS), "( )");
        element.removeAttribute(Parser.ATTR_INITCLASS);
        //try {
        try {
          if (st.hasMoreTokens()) {
            Class initClass = Class.forName(st.nextToken());      // load update type
            try {                                                  // look for a getInstance() methode
              Method factoryMethod = initClass.getMethod(Parser.GETINSTANCE, (Class[]) null);
              if (Modifier.isStatic(factoryMethod.getModifiers())) {
                initParameter = factoryMethod.invoke(null, (Object[]) null);
              }
            } catch (NoSuchMethodException nsme) {
              // really nothing to do here
            }
            if (initParameter == null && st.hasMoreTokens()) { // now try to instantiate with String taking ctor
              try {
                Constructor ctor = initClass.getConstructor(new Class[]{String.class});
                String pattern = st.nextToken();
                initParameter = ctor.newInstance(new Object[]{pattern});
              } catch (NoSuchMethodException e) {
              } catch (SecurityException e) {
              } catch (InstantiationException e) {
              } catch (IllegalAccessException e) {
              } catch (IllegalArgumentException e) {
              } catch (InvocationTargetException e) {
              }
            }
            if (initParameter == null) { // now try to instantiate with default ctor
              initParameter = initClass.newInstance();
            }
            if (Action.class.isInstance(initParameter)) {
              for (int i = 0, n = attributes.size(); i < n; i++) {
                Attribute attrib = (Attribute) attributes.get(i);
                String attribName = attrib.getName();
                if (attribName != null && attribName.startsWith(ATTR_MACOS_PREFIX)) {
                  mac_map.put(attribName, initParameter);
                }
              }
            }
          }
        } catch (ClassNotFoundException e) {
          System.err.println(Parser.ATTR_INITCLASS + " not instantiated : " + e.getLocalizedMessage() + e);
        } catch (SecurityException e) {
          System.err.println(Parser.ATTR_INITCLASS + " not instantiated : " + e.getLocalizedMessage() + e);
        } catch (IllegalAccessException e) {
          System.err.println(Parser.ATTR_INITCLASS + " not instantiated : " + e.getLocalizedMessage() + e);
        } catch (IllegalArgumentException e) {
          System.err.println(Parser.ATTR_INITCLASS + " not instantiated : " + e.getLocalizedMessage() + e);
        } catch (InvocationTargetException e) {
          System.err.println(Parser.ATTR_INITCLASS + " not instantiated : " + e.getLocalizedMessage() + e);
        } catch (InstantiationException e) {
          System.err.println(Parser.ATTR_INITCLASS + " not instantiated : " + e.getLocalizedMessage() + e);
        } catch (RuntimeException re) {
          throw re;
        } catch (Exception e) {
          throw new Exception(Parser.ATTR_INITCLASS + " not instantiated : " + e.getLocalizedMessage(), e);
        }
      }

      obj = initParameter != null ? factory.newInstance(new Object[]{initParameter}) : factory.newInstance();
      constructed = true;
      //
      //  put newly created object in the map if it has an <id> attribute (uniqueness is given att this point)
      //
      if (id != null) {
        engine.getIdMap().put(id, obj);
      }
    }
    //
    //  1st attempt to apply attributes (call setters on the objects)
    //    put an action attribute at the beginning of the attribute list
    //
    Attribute actionAttr = element.getAttribute("Action");
    if (actionAttr != null) {
      element.removeAttribute(actionAttr);
      attributes.add(0, actionAttr);
    }
    //
    //  put Tag's Text content into Text Attribute
    //
    if (element.getAttribute("Text") == null && 0 < element.getTextTrim().length()) {
      attributes.add(new Attribute("Text", element.getTextTrim()));
    }
    List remainingAttrs = applyAttributes(obj, factory, attributes);
    //
    //  process child tags
    //

    LayoutManager layoutMgr = obj instanceof Container ? ((Container) obj).getLayout() : null;

    Iterator it = element.getChildren().iterator();
    while (it != null && it.hasNext()) {
      Element child = (Element) it.next();
      //
      //  Prepare for possible groupping through BottonGroup Tag
      //
      if ("buttongroup".equalsIgnoreCase(child.getName())) {

        int k = JMenu.class.isAssignableFrom(obj.getClass()) ? ((JMenu) obj).getItemCount() : ((Container) obj).getComponentCount();
        getSwing(child, obj);
        int n = JMenu.class.isAssignableFrom(obj.getClass()) ? ((JMenu) obj).getItemCount() : ((Container) obj).getComponentCount();
        //
        //  add the recently add container entries into the btngroup
        //
        ButtonGroup btnGroup = new ButtonGroup();
        // incase the button group was given an id attr. it should also be put into the idmap.
        if (null != child.getAttribute(Parser.ATTR_ID)) {
          engine.getIdMap().put(child.getAttribute(Parser.ATTR_ID).getValue(), btnGroup);
        }
        while (k < n) {
          putIntoBtnGrp(JMenu.class.isAssignableFrom(obj.getClass()) ? ((JMenu) obj).getItem(k++) : ((Container) obj).getComponent(k++), btnGroup);
        }
        continue;
      }

      //
      //  A CONSTRAINTS attribute is removed from the childtag but used to add the child into the currrent obj
      //
      Attribute constrnAttr = child.getAttribute(Parser.ATTR_CONSTRAINTS);
      Object constrains = null;
      if (constrnAttr != null && layoutMgr != null) {
        child.removeAttribute(Parser.ATTR_CONSTRAINTS); // therefore it won't be used in getSwing(child)
        constrains = ConstraintsConverter.convert(layoutMgr.getClass(), constrnAttr);
      }

      //
      //  A GridBagConstraints grand-childtag is not added at all ..
      //  .. but used to add the child into this container
      //
      Element grandchild = child.getChild("gridbagconstraints");
      if (grandchild != null) {
        addChild((Container) obj, (Component) getSwing(child, null), getSwing(grandchild, null));
      } else if (!child.getName().equals("gridbagconstraints")) {
        addChild((Container) obj, (Component) getSwing(child, null), constrains);
      }
    }

    //
    //  2nd attempt to apply attributes (call setters on the objects)
    //
    if (remainingAttrs != null && 0 < remainingAttrs.size()) {
      remainingAttrs = applyAttributes(obj, factory, remainingAttrs);
      if (remainingAttrs != null) {
        it = remainingAttrs.iterator();
        while (it != null && it.hasNext()) {
          Attribute attr = (Attribute) it.next();
          if (JComponent.class.isAssignableFrom(obj.getClass())) {
            ((JComponent) obj).putClientProperty(attr.getName(), attr.getValue());
            if (SwingEngine.DEBUG_MODE) {
              System.out.println("ClientProperty put: " + obj.getClass().getName() + "(" + id + "): " + attr.getName() + "=" + attr.getValue());
            }
          } else {
            if (SwingEngine.DEBUG_MODE) {
              System.err.println(attr.getName() + " not applied for tag: <" + element.getName() + ">");
            }
          }
        }
      }
    }

    return (constructed ? obj : null);
  }

  /**
   * Creates an object and sets properties based on the XML tag's attributes
   *
   * @param obj        <code>Object</code> object representing a tag found in the SWIXML descriptor document
   * @param factory    <code>Factory</code> factory to instantiate a new object
   * @param attributes <code>List</code> attribute list
   * @return <code>List</code> - list of attributes that could not be applied.
   * @throws Exception <pre>
   *                   <ol>
   *                   <li>For every attribute, createContainer() 1st tries to find a setter in the given factory.<br>
   *                   if a setter can be found and converter exists to convert the parameter string into a type that fits
   *                   the setter method, the setter gets invoked.</li>
   *                   <li>Otherwise, createContainer() looks for a public field with a matching name.</li>
   *                   </ol>
   *                   </pre><pre>
   *                   <b>Example:</b><br>
   *                   <br>1.) try to create a parameter obj using the ParameterFactory: i.e.
   *                   <br>background="FFC9AA" = container.setBackground ( new Color(attr.value) )
   *                   <br>2.) try to find a simple setter taking a primitive or String:  i.e.
   *                   <br>width="25" container.setWidth( new Interger( attr. getIntValue() ) )
   *                   <br>3.) try to find a public field,
   *                   <br>container.BOTTOM_ALIGNMENT
   *                   </pre>
   */
  private List applyAttributes(Object obj, Factory factory, List attributes) throws Exception {
    //
    // pass 1: Make an 'action' the 1st attribute to be processed -
    // otherwise the action would overwrtite already applied attributes like text etc.
    //

    for (int i = 0; i < attributes.size(); i++) {
      Attribute attr = (Attribute) attributes.get(i);
      if (Parser.ATTR_ACTION.equalsIgnoreCase(attr.getName())) {
        attributes.remove(i);
        attributes.add(0, attr);
        break;
      }
    }

    //
    //  pass 2: process the attributes
    //

    Iterator it = attributes.iterator();
    List list = new ArrayList();  // remember not applied attributes
    Action action = null; // used to insert an action into the macmap

    while (it != null && it.hasNext()) { // loop through all available attributes
      Attribute attr = (Attribute) it.next();

      if (Parser.ATTR_ID.equals(attr.getName()))
        continue;
      if (Parser.ATTR_REFID.equals(attr.getName()))
        continue;
      if (Parser.ATTR_USE.equals(attr.getName()))
        continue;

      if (action != null && attr.getName().startsWith(Parser.ATTR_MACOS_PREFIX)) {
        mac_map.put(attr.getName(), action);
        continue;
      }

      if (JLabel.class.isAssignableFrom(obj.getClass()) && attr.getName().equalsIgnoreCase("LabelFor")) {
        lbl_map.put(obj, attr.getValue());
        continue;
      }

      // using guessSetter allow to not have to care about case when looking at the attribute name.
      Method method = factory.guessSetter(attr.getName());
      if (method != null) {
        //
        //  A setter method has successfully been identified.
        //
        Class paraType = method.getParameterTypes()[0];
        Converter converter = cvtlib.getConverter(paraType);

        if (converter != null) {  // call setter with a newly instanced parameter
          Object para = null;
          try {
            //
            //  Actions are provided in the engine's member variables.
            //  a getClass().getFields lookup has to be done to find the correct fields.
            //
            if (Action.class.equals(paraType)) {
              para = engine.getClient().getClass().getField(attr.getValue()).get(engine.getClient());
              action = (Action) para;
            } else {
              para = converter.convert(paraType, attr, engine.getLocalizer());
            }

            method.invoke(obj, new Object[]{para}); // ATTR SET

          } catch (NoSuchFieldException e) {
            if (SwingEngine.DEBUG_MODE) {
              System.err.println("Action '" + attr.getValue() + "' not set. Public Action '" + attr.getValue() + "' not found in " + engine.getClient().getClass().getName());
            }
          } catch (InvocationTargetException e) {
            //
            // The JFrame class is slightly incompatible with Frame.
            // Like all other JFC/Swing top-level containers, a JFrame contains a JRootPane as its only child.
            // The content pane provided by the root pane should, as a rule, contain all the non-menu components
            // displayed by the JFrame. The JFrame class is slightly incompatible with Frame.
            //
            if (obj instanceof RootPaneContainer) {
              Container rootpane = ((RootPaneContainer) obj).getContentPane();
              Factory f = engine.getTaglib().getFactory(rootpane.getClass());
              Method m = f.guessSetter(attr.getName());
              try {
                m.invoke(rootpane, new Object[]{para}); // ATTR SET
              } catch (Exception ex) {
                list.add(attr);
              }
            } else {
              list.add(attr);
            }
          } catch (Exception e) {
            throw new Exception(e + ":" + method.getName() + ":" + para, e);
          }
          continue;
        }

        //
        // try this: call the setter with an Object.class Type
        //
        if (paraType.equals(Object.class)) {
          try {
            String s = attr.getValue();
            if (Parser.LOCALIZED_ATTRIBUTES.contains(attr.getName().toLowerCase()) && attr.getAttributeType() == Attribute.CDATA_TYPE) {
              s = engine.getLocalizer().getString(s);
            }
            method.invoke(obj, new Object[]{s}); // ATTR SET
          } catch (Exception e) {
            list.add(attr);
          }
          continue;
        }

        //
        // try this: call the setter with a primitive
        //
        if (paraType.isPrimitive()) {
          try {
            method.invoke(obj, new Object[]{PrimitiveConverter.conv(paraType, attr, engine.getLocalizer())}); // ATTR SET
          } catch (Exception e) {
            list.add(attr);
          }
          continue;
        }
        //
        // try again later
        //
        list.add(attr);
        continue;
      } else {
        //
        //  Search for a public field in the obj.class that matches the attribute name
        //
        try {
          Field field = obj.getClass().getField(attr.getName());
          if (field != null) {
            Converter converter = cvtlib.getConverter(field.getType());
            if (converter != null) {
              //
              //  Localize Strings
              //
              Object fieldValue = converter.convert(field.getType(), attr, null);
              if (String.class.equals(converter.convertsTo())) {
                fieldValue = engine.getLocalizer().getString((String) fieldValue);
              }
              field.set(obj, fieldValue);  // ATTR SET
            } else {
              list.add(attr);
            }
          } else {
            list.add(attr);
          }
        } catch (Exception e) {
          list.add(attr);
        }
      }
    } // end_while
    return list;
  }


  /**
   * Copies attributes that element doesn't have yet form element[id]
   *
   * @param target <code>Element</code> target to receive more attributes
   */
  private void cloneAttributes(Element target) {
    Element source = null;
    if (target.getAttribute(Parser.ATTR_REFID) != null) {
      source = find(jdoc.getRootElement(), target.getAttribute(Parser.ATTR_REFID).getValue().trim());
    } else if (target.getAttribute(Parser.ATTR_USE) != null) {
      source = find(jdoc.getRootElement(), target.getAttribute(Parser.ATTR_USE).getValue().trim());
    }
    if (source != null) {
      Iterator it = source.getAttributes().iterator();
      while (it != null && it.hasNext()) {
        Attribute attr = (Attribute) it.next();
        String name = attr.getName().trim();
        //
        //  copy but don't overwrite an attr.
        //  also, don't copy the id attr.
        //
        if (!Parser.ATTR_ID.equals(name) && target.getAttribute(name) == null) {
          Attribute attrcln = (Attribute) attr.clone();
          attrcln.detach();
          target.setAttribute(attrcln);
        }
      } // end while
    }
  }


  /**
   * Adds a child component to a parent component
   * considering many differences between the Swing containers
   *
   * @param parent     <code>Component</code>
   * @param component  <code>Component</code> child to be added to the parent
   * @param constrains <code>Object</code> contraints
   * @return <code>Component</code> - the passed in component
   */
  private static Component addChild(Container parent, Component component, Object constrains) {
    if (component == null)
      return null;
    //
    //  Set a JMenuBar for JFrames, JDialogs, etc.
    //
    if (component instanceof JMenuBar) {

      try {
        Method m = parent.getClass().getMethod("setJMenuBar", new Class[]{JMenuBar.class});
        m.invoke(parent, new Object[]{component});
      } catch (NoSuchMethodException e) {
        parent.add(component);
      } catch (Exception e) {
        // intentionally empty
      }
 
    } else if (parent instanceof RootPaneContainer) {
      //
      //  add component into RootContainr
      //  All Swing top-level containers contain a JRootPane as their only child.
      //  The content pane provided by the root pane should contain all the non-menu components.
      //
      RootPaneContainer rpc = (RootPaneContainer) parent;
      if (component instanceof LayoutManager) {
        rpc.getContentPane().setLayout((LayoutManager) component);
      } else {
        rpc.getContentPane().add(component, constrains);
      }
    } else if (parent instanceof JScrollPane) {
      //
      //  add component into a ScrollPane
      //
      JScrollPane scrollPane = (JScrollPane) parent;
      scrollPane.setViewportView(component);
    } else if (parent instanceof JSplitPane) {
      //
      //  add component into a SplitPane
      //
      JSplitPane splitPane = (JSplitPane) parent;
      if (splitPane.getOrientation() == JSplitPane.HORIZONTAL_SPLIT) {
        //
        //  Horizontal SplitPane
        //
        if (splitPane.getTopComponent() == null) {
          splitPane.setTopComponent(component);
        } else {
          splitPane.setBottomComponent(component);
        }
      } else {
        //
        //  Vertical SplitPane
        //
        if (splitPane.getLeftComponent() == null) {
          splitPane.setLeftComponent(component);
        } else {
          splitPane.setRightComponent(component);
        }
      }
    } else if (parent instanceof JMenuBar && component instanceof JMenu) {
      //
      //  add Menu into a MenuBar
      //
      JMenuBar menuBar = (JMenuBar) parent;
      menuBar.add(component, constrains);
    } else if (JSeparator.class.isAssignableFrom(component.getClass())) {
      //
      //  add Separator to a Menu, Toolbar or PopupMenu
      //
      if (JToolBar.class.isAssignableFrom(parent.getClass()))
        ((JToolBar) parent).addSeparator();
      else if (JPopupMenu.class.isAssignableFrom(parent.getClass()))
        ((JPopupMenu) parent).addSeparator();
      else if (JMenu.class.isAssignableFrom(parent.getClass()))
        ((JMenu) parent).addSeparator();
      else if (constrains != null)
        parent.add(component, constrains);
      else
        parent.add(component);

    } else if (parent instanceof Container) {
      //
      //  add compontent into container
      //
      if (constrains == null) {
        parent.add(component);
      } else {
        parent.add(component, constrains);
      }
    }
    return component;
  }


  /**
   * Moves the content from the source into the traget <code>Element</code>
   *
   * @param source <code>Element</code> Content provider
   * @param target <code>Element</code> Content receiver
   */
  private static void moveContent(Element source, Element target) {
    List list = source.getContent();
    while (!list.isEmpty()) {
      Object obj = list.remove(0);
      target.getContent().add(obj);
    }
  }

  /**
   * Recursive element by id finder
   *
   * @param element <code>Element</code> start node
   * @param id      <code>String</code> id to look for
   * @return <code>Element</code> - with the given id in the id attribute or null if not found
   */
  private static Element find(Element element, String id) {
    Element elem = null;
    Attribute attr = element.getAttribute(Parser.ATTR_ID);
    if (attr != null && id.equals(attr.getValue().trim())) {
      elem = element;
    } else {
      Iterator it = element.getChildren().iterator();
      while (it != null && it.hasNext() && elem == null) {
        elem = find((Element) it.next(), id.trim());
      }
    }
    return elem;
  }

  /**
   * Recursively adds AbstractButtons into the given
   *
   * @param obj <code>Object</code> should be an AbstractButton or JComponent containing AbstractButtons
   * @param grp <code>ButtonGroup</code>
   */
  private static void putIntoBtnGrp(Object obj, ButtonGroup grp) throws Exception {
    if (AbstractButton.class.isAssignableFrom(obj.getClass())) {
      grp.add((AbstractButton) obj);
    } else if (JComponent.class.isAssignableFrom(obj.getClass())) {
      JComponent jp = (JComponent) obj;
      for (int i = 0; i < jp.getComponentCount(); i++) {
        putIntoBtnGrp(jp.getComponent(i), grp);
      }
    } // otherwise just do nothing ...
  }
}
