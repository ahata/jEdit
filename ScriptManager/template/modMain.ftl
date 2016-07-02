<#--
変数一覧
systemName = 太陽殿次期生産管理システム
specCreateDate = 2012/8/30
specAuthor = 神原
specVersion = 1.00
genDate = new Date()
progID = E060
-->
'*******************************************************
'システム名：${systemName}
'仕様作成日：${specCreateDate}
'仕様作成者：${specAuthor}
'仕様改版数：${specVersion}
'生成日時：${genDate?string("yyyy/MM/dd HH:mm:ss")}
'*******************************************************
Imports ISOP
Imports ISOPDb
Imports ISOPForms

'******************************************************
'PROGRAM：${progID}
'------------------------------------------------------
'ﾒﾝﾃﾅﾝｽ記録
'${genDate?string("yyyy年MM月dd日")}：新規作成
'******************************************************
Module modEntry
    '共通ﾓｼﾞｭｰﾙ
    Friend App As New ISOPApplication()

    'メイン画面
    Friend MainForm As frm${progID}01 = Nothing

    '******************************************************
    'MAIN ENTRY:PROGRAM：${progID}
    '******************************************************
    Sub Main()
        Try
            App.StartApplication(ISOP.ApplicationType.Client)

            MainForm = New frm${progID}01()

            Application.Run(MainForm)
            App.ExitApplication()
        Catch ex As Exception
            App.Abort(-1, ex)
        End Try
    End Sub
End Module

