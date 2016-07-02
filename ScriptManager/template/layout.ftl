<#--
画面項目仕様書より、レイアウト生成
-->
<#setting number_format="#">
<Global.Microsoft.VisualBasic.CompilerServices.DesignerGenerated()> _
Partial Class ${form.name}
    Inherits ISOPForms.ISOPForm

    'フォームがコンポーネントの一覧をクリーンアップするために dispose をオーバーライドします。
    <System.Diagnostics.DebuggerNonUserCode()> _
    Protected Overrides Sub Dispose(ByVal disposing As Boolean)
        Try
            If disposing AndAlso components IsNot Nothing Then
                components.Dispose()
            End If
        Finally
            MyBase.Dispose(disposing)
        End Try
    End Sub

    'Windows フォーム デザイナで必要です。
    Private components As System.ComponentModel.IContainer

    'メモ: 以下のプロシージャは Windows フォーム デザイナで必要です。
    'Windows フォーム デザイナを使用して変更できます。  
    'コード エディタを使って変更しないでください。
    <System.Diagnostics.DebuggerStepThrough()> _
    Private Sub InitializeComponent()
<#list components as cp>
        Me.${cp.name} = New System.Windows.Forms.${cp.type}
</#list>
<#list form.controls as c>
        <@control c=c />
</#list>
        '
        '${form.name}
        '
        Me.AutoScaleDimensions = New System.Drawing.SizeF(6.0!, 12.0!)
        Me.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font
        Me.BackColor = System.Drawing.Color.FromArgb(CType(CType(224, Byte), Integer), CType(CType(224, Byte), Integer), CType(CType(224, Byte), Integer))
        Me.ClientSize = New System.Drawing.Size(${form.width}, ${form.height})
<#list form.controls as ctl>
        Me.Controls.Add(Me.${ctl.name})
</#list>
        Me.MaximizeBox = False
        Me.Name = "${form.name}"
        Me.StartPosition = System.Windows.Forms.FormStartPosition.CenterScreen
        Me.Text = "${form.text}"
        Me.ResumeLayout(False)

    End Sub
<#list components as cp>
    Friend WithEvents ${cp.name} As System.Windows.Forms.${cp.type}
</#list>
End Class
<#--
Panel
-->
<#macro control c>
        '
        '${c.name}
        '
  <#if c.controls?? >
    <#list c.controls as ctl>
        Me.${c.name}.Controls.Add(Me.${ctl.name})
    </#list>
        Me.${c.name}.Dock = System.Windows.Forms.DockStyle.${c.dock}
        Me.${c.name}.Location = New System.Drawing.Point(${c.left}, ${c.top})
        Me.${c.name}.Name = "${c.name}"
        Me.${c.name}.Size = New System.Drawing.Size(${c.width}, ${c.height})
        Me.${c.name}.TabIndex = 1
    <#list c.controls as ctl>
        <@control c=ctl />
    </#list>
  <#else>
    <#switch c.type>
      <#case "Button">
        <@button btn=c />
        <#break>
      <#case "TextBox">
      <#case "ComboBox">
        <@text txt=c />
        <#break>
      <#case "Label">
        <@label lbl=c />
    </#switch>
  </#if>
</#macro>
<#--
Button
-->
<#macro button btn>
        Me.${btn.name}.Font = New System.Drawing.Font("ＭＳ ゴシック", 9.0!, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, CType(128, Byte))
        Me.${btn.name}.Location = New System.Drawing.Point(${btn.left}, ${btn.top})
        Me.${btn.name}.Name = "${btn.name}"
        Me.${btn.name}.Size = New System.Drawing.Size(${btn.width}, ${btn.height})
        Me.${btn.name}.TabIndex = 1
        Me.${btn.name}.Text = "${btn.text}"
        Me.${btn.name}.UseVisualStyleBackColor = True
</#macro>
<#--
Text/ComboBox
-->
<#macro text txt>
        Me.${txt.name}.Font = New System.Drawing.Font("ＭＳ ゴシック", 10.0!, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, CType(128, Byte))
        Me.${txt.name}.Location = New System.Drawing.Point(${txt.left}, ${txt.top})
        Me.${txt.name}.Name = "${txt.name}"
        Me.${txt.name}.Size = New System.Drawing.Size(${txt.width}, ${txt.height})
        Me.${txt.name}.TabIndex = 1
</#macro>
<#--
Label
-->
<#macro label lbl>
        Me.${lbl.name}.BackColor = System.Drawing.Color.Blue
        Me.${lbl.name}.BorderStyle = System.Windows.Forms.BorderStyle.FixedSingle
        Me.${lbl.name}.Font = New System.Drawing.Font("ＭＳ ゴシック", 10.0!)
        Me.${lbl.name}.ForeColor = System.Drawing.Color.White
        Me.${lbl.name}.ImageAlign = System.Drawing.ContentAlignment.MiddleRight
        Me.${lbl.name}.Location = New System.Drawing.Point(${lbl.left}, ${lbl.top})
        Me.${lbl.name}.Name = "${lbl.name}"
        Me.${lbl.name}.Size = New System.Drawing.Size(${lbl.width}, ${lbl.height})
        Me.${lbl.name}.TabIndex = 1
        Me.${lbl.name}.Text = "${lbl.text}"
        Me.${lbl.name}.TextAlign = System.Drawing.ContentAlignment.MiddleLeft
</#macro>
