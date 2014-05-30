/*
 * Copyright 2014  IBM Corp.
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing permissions and limitations under the
 * License.
 */
package com.ibm.sample.wcm.healthcheck;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.SimpleTagSupport;

public class HealthCheckTag extends SimpleTagSupport {
  private String library;
  private String textItem;
  private String update;
  private String restUrl;
  private String vpContext;


  public HealthCheckTag() {
  }

  @Override
  public void doTag() throws JspException, IOException {
    HealthContent hc = new HealthContent(library, textItem, vpContext);
    if (Boolean.parseBoolean(update)) {
      hc.update();
    }
    if (Boolean.parseBoolean(restUrl)) {
        getJspContext().getOut().print(hc.genRestUrl());
    }
    else {
      getJspContext().getOut().print(hc.check());
    }
  }

  public String getLibrary() {
    return library;
  }

  public void setLibrary(String library) {
    this.library = library;
  }

  public String getTextItem() {
    return textItem;
  }

  public void setTextItem(String textItem) {
    this.textItem = textItem;
  }

  public String getUpdate() {
    return update;
  }

  public void setUpdate(String update) {
    this.update = update;
  }

  public String getRestUrl() {
    return restUrl;
  }

  public void setRestUrl(String restUrl) {
    this.restUrl = restUrl;
  }

  public String getVpContext() {
    return vpContext;
  }

  public void setVpContext(String vpContext) {
    this.vpContext = vpContext;
  }

}
