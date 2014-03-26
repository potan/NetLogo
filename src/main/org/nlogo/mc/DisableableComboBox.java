// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.mc;

import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import java.awt.Component;


class Item<T> {

  private T obj;
  private boolean enabled;

  public Item(T obj, boolean enabled) {
    this.obj = obj;
    this.enabled = enabled;
  }

  public T getObj() {
    return obj;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public String toString() {
    return obj.toString();
  }

}

public strictfp class DisableableComboBox<T> extends JComboBox<Item<T> > {

  public DisableableComboBox() {
    super();
    setRenderer(new BasicComboBoxRenderer() {
      public Component getListCellRendererComponent(JList<Item<T> > list, Item<T> value, int index, boolean isSelected, boolean cellHasFocus) {
        Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if(value != null && !value.isEnabled()) {
          component.setEnabled(false);
          component.setForeground(UIManager.getColor("ComboBox.disabledForeground"));
          component.setVisible(false);
        } else {
          component.setEnabled(true);
          component.setVisible(true);
        }
        return component;
      }
    });
  }

  public int addItem(T anObject, boolean isObjectEnabled) {
    Item<T> item = new Item<T>(anObject, isObjectEnabled);
    super.addItem(item);
    return getItemCount() - 1;
  }

  public void setIndexEnabled(int index, boolean isObjectEnabled) {
    if(index >= 0 && index < getItemCount()) {
      Item<T> item = getItemAt(index);
      item.setEnabled(isObjectEnabled);
    }
  }

  @Override
  public void setSelectedIndex(int i) {
    if(i >= 0 && i < getItemCount() && !( ((Item)(getItemAt(i))).isEnabled())  ) {
      return;
    }
    super.setSelectedIndex(i);
  }

  //Use getSelectedObject instead of getSelectedItem
  //Overriding getSelectedObject causes problems since internally JComboBox would expect getSelectedItem to return a
  //type Item
  public T getSelectedObject() {
    Item<T> selectedItem = (Item<T>)super.getSelectedItem();
    if(selectedItem == null) {
      return null;
    } else {
      return selectedItem.getObj();
    }
  }

}

