<#import "/resource/common_html_front.ftl" as html>
<#import "/indexMenu.ftl" as menu>

<@html.htmlBase title="选择支付">
    <@menu.menu selectMenu=""/>
<style type="text/css">
    .align-center{
        margin:0 auto; /* 居中 这个是必须的，，其它的属性非必须 */
        width:500px; /* 给个宽度 顶到浏览器的两边就看不出居中效果了 */
        text-align:center; /* 文字等内容居中 */
    }
</style>
<div class="align-center">
<h3>订单支付选择</h3>
<table class="table">
    <tr>
        <td>订单号:</td>
        <td>${order.id!""}</td>
    </tr>
    <tr>
        <td>商品名称 :</td>
        <td>${order.remark!""}</td>
    </tr>
    <tr>
        <td>金额:</td>
        <td>${order.amount!""}</td>
    </tr>
    <tr>
        <td>配送费:</td>
        <td>${order.logistics_fee!""}</td>
    </tr>
    <tr>
        <td>总金额:</td>
        <td>${order.ptotal!""}</td>
    </tr>
</table>
<#--<div> <input id="btnPay" type="button" class="btn btn-primary" value="确认支付"/></div>-->
    <div>
        <a href="${basepath}/pay/pcpay?orderId=${order.id!""}"><img src="${basepath}/resource/images/zfbzf.jpg" width="100" height="50"></a>
        <a href="${basepath}/pay/wxpay?orderId=${order.id!""}"><img src="${basepath}/resource/images/wxzf.jpg" width="100" height="50"></a>
    </div>
       <#-- <input id="btnPay" type="button" class="btn btn-primary" value="确认支付"/></div>-->
</div>
<script>
    $(function(){
        $("#btnPay").click(function(){
            if(confirm("确认支付?")) {
                $.ajax({
                    dataType:"json",
                    url:"${basepath}/paygate/dummyPay",
                    type:"POST",
                    data:{orderId:"${payInfo.WIDout_trade_no}"},
                    success:function(data){
                        window.location.href="${basepath}/order/paySuccess.html";
                    },
                    error:function(data){
                        alert("支付失败");
                    }
                });
            }
        });
    })
</script>
<#--<script>
    $(function(){
        $("#btnPay").click(function(){
            if(confirm("确认支付?")) {
                $.ajax({
                    dataType:"json",
                    url:"${basepath}/paygate/dummyPay",
                    type:"POST",
                    data:{orderId:"${payInfo.WIDout_trade_no}"},
                    success:function(data){
                        window.location.href="${basepath}/order/paySuccess.html";
                    },
                    error:function(data){
                        alert("支付失败");
                    }
                });
            }
        });
    })
</script>-->
</@html.htmlBase>