// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.shape.editor;

import org.nlogo.api.I18N;
import org.nlogo.api.Shape;
import org.nlogo.api.ShapeList;
import org.nlogo.shape.LinkShape;

strictfp class LinkEditorDialog
    extends javax.swing.JDialog
    implements EditorDialog.VectorShapeContainer {
  private final javax.swing.JTextField name = new javax.swing.JTextField(10);
  private final javax.swing.JTextField curviness = new javax.swing.JTextField(10);

  private final java.util.Vector<javax.swing.JComboBox<Short> > dashes = new java.util.Vector<javax.swing.JComboBox<Short> >(3);

  private final LinkShape shape;
  private final LinkShape originalShape;
  private final DrawableList list;

  public void update(Shape originalShape, Shape newShape) {
    shape.setDirectionIndicator((org.nlogo.shape.VectorShape) newShape);
  }

  public boolean exists(String name) {
    return false;
  }

  LinkEditorDialog(final DrawableList list, final LinkShape shape, int x, int y) {
    super((javax.swing.JFrame) null, true);
    this.originalShape = shape;
    this.shape = (LinkShape) shape.clone();
    this.list = list;
    setResizable(false);
    setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
    addWindowListener(
        new java.awt.event.WindowAdapter() {
          @Override
          public void windowClosing(java.awt.event.WindowEvent e) {
            saveShape();
          }
        });

    org.nlogo.swing.Utils.addEscKeyAction
        (this,
            new javax.swing.AbstractAction() {
              public void actionPerformed(java.awt.event.ActionEvent e) {
                if (!originalShape.toString().equals(getCurrentShape().toString())
                    && 0 != javax.swing.JOptionPane.showConfirmDialog
                    (LinkEditorDialog.this,
                        "You may lose changes made to this shape. Do you want to cancel anyway?",
                        "Confirm Cancel", javax.swing.JOptionPane.YES_NO_OPTION)) {
                  return;
                }
                dispose();
              }
            });

    java.awt.GridBagLayout gb = new java.awt.GridBagLayout();
    java.awt.GridBagConstraints c = new java.awt.GridBagConstraints();
    setLayout(gb);

    javax.swing.JLabel label = new javax.swing.JLabel("name: ");
    c.anchor = java.awt.GridBagConstraints.WEST;
    add(label, c);
    c.gridwidth = java.awt.GridBagConstraints.REMAINDER;
    name.setText(shape.getName());
    add(name, c);

    c.gridwidth = 1;
    label = new javax.swing.JLabel("direction indicator: ");
    add(label, c);
    c.gridwidth = java.awt.GridBagConstraints.REMAINDER;
    javax.swing.JButton diButton = new javax.swing.JButton("Edit");
    add(diButton, c);
    diButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent e) {
        new EditorDialog
            (LinkEditorDialog.this, (org.nlogo.shape.VectorShape) shape.getDirectionIndicator(),
                getLocation().x, getLocation().y, false);
      }
    });

    c.gridwidth = 1;
    label = new javax.swing.JLabel("curviness: ");
    add(label, c);
    c.gridwidth = java.awt.GridBagConstraints.REMAINDER;
    curviness.setText(Double.toString(shape.curviness()));
    add(curviness, c);

    Short dashChoices[] = new Short[5];
    dashChoices[0] = new Short((short)0x0000);
    dashChoices[1] = new Short((short)0xffff);
    dashChoices[2] = new Short((short)0xf0f0);
    dashChoices[3] = new Short((short)0xcccc);
    dashChoices[4] = new Short((short)0xcaca);
    for (int i = 0; i < dashes.size(); i++) {
      javax.swing.JComboBox<Short> b = new javax.swing.JComboBox<Short>(dashChoices);
      b.setRenderer(new DashCellRenderer());
      b.setSelectedItem(shape.getDashes(i));
      dashes.set(i,b);
    }

    c.gridwidth = 1;
    add(new javax.swing.JLabel("left line"), c);
    c.gridwidth = java.awt.GridBagConstraints.REMAINDER;
    add(dashes.get(2), c);

    c.gridwidth = 1;
    add(new javax.swing.JLabel("middle line"), c);
    c.gridwidth = java.awt.GridBagConstraints.REMAINDER;
    add(dashes.get(1), c);

    c.gridwidth = 1;
    add(new javax.swing.JLabel("right line"), c);
    c.gridwidth = java.awt.GridBagConstraints.REMAINDER;
    add(dashes.get(0), c);

    javax.swing.JButton cancel = new javax.swing.JButton(I18N.guiJ().get("common.buttons.cancel"));
    cancel.addActionListener
        (new java.awt.event.ActionListener() {
          public void actionPerformed(java.awt.event.ActionEvent e) {
            dispose();
          }
        });

    javax.swing.JButton done = new javax.swing.JButton(I18N.guiJ().get("common.buttons.ok"));
    done.addActionListener
        (new java.awt.event.ActionListener() {
          public void actionPerformed(java.awt.event.ActionEvent e) {
            saveShape();
            setVisible(false);
            dispose();
          }
        });

    javax.swing.JPanel buttonPanel = new org.nlogo.swing.ButtonPanel
        (new javax.swing.JButton[]{done, cancel});

    c.anchor = java.awt.GridBagConstraints.EAST;
    add(buttonPanel, c);
    setLocation(x + 10, y + 10);

    setTitle("Link Shape");
    name.setEnabled(!ShapeList.isDefaultShapeName(shape.getName()));

    list.update();
    pack();
    getRootPane().setDefaultButton(done);
    // when name is not enabled focus goes to the curviness
    // field instead ev 2/18/08
    if (ShapeList.isDefaultShapeName(shape.getName())) {
      curviness.requestFocus();
    } else {
      name.requestFocus();
    }
    setVisible(true);
  }

  void saveShape() {
    String nameStr;
    // Make sure the shape has a name
    if (name.getText().equals("")) {
      nameStr =
          javax.swing.JOptionPane.showInputDialog
              (this, "Name:", "Name Shape", javax.swing.JOptionPane.PLAIN_MESSAGE);
      if (nameStr == null) {
        return;
      }
    } else {
      nameStr = name.getText();
    }

    nameStr = nameStr.toLowerCase();

    shape.setName(nameStr);

    String originalName = originalShape.getName();
    // If this is an attempt to overwrite a shape, prompt for
    // permission to do it
    if (list.exists(nameStr)
        && !nameStr.equals(originalName)
        && javax.swing.JOptionPane.YES_OPTION != javax.swing.JOptionPane.showConfirmDialog
        (this, "A shape with this name already exists. Do you want to replace it?",
            "Confirm Overwrite", javax.swing.JOptionPane.YES_NO_OPTION)) {
      return;
    }

    double cv = 0;
    String str = curviness.getText();

    while (str != null) {
      try {
        cv = Double.parseDouble(str);
        str = null;
      } catch (NumberFormatException e) {
        str =
            javax.swing.JOptionPane.showInputDialog
                (this, "Curviness:", "Enter a number", javax.swing.JOptionPane.PLAIN_MESSAGE);
      }
    }

    shape.curviness(cv);
    for (int i = 0; i < dashes.size(); i++) {
      int index = dashes.get(i).getSelectedIndex();
      shape.setLineVisible(i, index != 0);
      shape.setDashiness(i, org.nlogo.shape.LinkLine.dashChoices[index]);
    }

    list.update(originalShape, shape);
    dispose();
  }

  private LinkShape getCurrentShape() {
    LinkShape currentShape = (LinkShape) shape.clone();
    currentShape.setName(name.getText());
    currentShape.curviness(Double.parseDouble(curviness.getText()));
    for (int i = 0; i < dashes.size(); i++) {
      int index = dashes.get(i).getSelectedIndex();
      currentShape.setLineVisible(i, index != 0);
      currentShape.setDashiness(i, org.nlogo.shape.LinkLine.dashChoices[index]);
    }
    return currentShape;
  }

//  private class DashCellRenderer
//      implements javax.swing.ListCellRenderer<float[]> {
//    public java.awt.Component getListCellRendererComponent
//        (javax.swing.JList<? extends float[]> list, float[] value,
//         int index, final boolean isSelected, boolean cellHasFocus) {
//      final float[] obj = value;
//
//      return new java.awt.Component() {
//        private final java.awt.Dimension dimension =
//            new java.awt.Dimension(85, 18);
//
//        @Override
//        public java.awt.Dimension getMinimumSize() {
//          return dimension;
//        }
//
//        @Override
//        public java.awt.Dimension getPreferredSize() {
//          return dimension;
//        }
//
//        @Override
//        public java.awt.Dimension getMaximumSize() {
//          return dimension;
//        }
//
//        @Override
//        public void paint(java.awt.Graphics g) {
//          float[] arry = (float[]) obj;
//          // this is a horrible hack. This configuration is supposed
//          // to be blank but for some reason on Windows it's not so just
//          // don't draw anything. ev 9/14/07
//          if (arry.length == 2 &&
//              arry[0] == 0 &&
//              arry[1] == 1) {
//            return;
//          }
//          java.awt.Graphics2D g2 = (java.awt.Graphics2D) g;
//          java.awt.Dimension d = getMinimumSize();
//          g2.setColor(java.awt.Color.black);
//          g2.setStroke(new java.awt.BasicStroke
//              (1.0f, java.awt.BasicStroke.CAP_ROUND,
//                  java.awt.BasicStroke.JOIN_ROUND, 1.0f, (float[]) obj, 0));
//          g2.drawLine(0, d.height / 2, d.width, d.height / 2);
//        }
//      };
//    }
//  }
  private class DashCellRenderer
      implements javax.swing.ListCellRenderer<Short> {
    public java.awt.Component getListCellRendererComponent
        (javax.swing.JList<? extends Short> list, Short value,
         int index, final boolean isSelected, boolean cellHasFocus) {

      return new java.awt.Component() {
        private final java.awt.Dimension dimension =
            new java.awt.Dimension(85, 18);

        @Override
        public java.awt.Dimension getMinimumSize() {
          return dimension;
        }

        @Override
        public java.awt.Dimension getPreferredSize() {
          return dimension;
        }

        @Override
        public java.awt.Dimension getMaximumSize() {
          return dimension;
        }

        @Override
        public void paint(java.awt.Graphics g) {
//          float[] arry = (float[]) obj;
          // this is a horrible hack. This configuration is supposed
          // to be blank but for some reason on Windows it's not so just
          // don't draw anything. ev 9/14/07
//          if (arry.length == 2 &&
//              arry[0] == 0 &&
//              arry[1] == 1) {
//            return;
//          }
//          java.awt.Graphics2D g2 = (java.awt.Graphics2D) g;
//          java.awt.Dimension d = getMinimumSize();
//          g2.setColor(java.awt.Color.black);
//          g2.setStroke(new java.awt.BasicStroke
//              (1.0f, java.awt.BasicStroke.CAP_ROUND,
//                  java.awt.BasicStroke.JOIN_ROUND, 1.0f, (float[]) obj, 0));
//          g2.drawLine(0, d.height / 2, d.width, d.height / 2);
        }
      };
    }
  }
}
