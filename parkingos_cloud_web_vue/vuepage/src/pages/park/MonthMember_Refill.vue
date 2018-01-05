<template>
    <section>
        <common-table
                :queryapi="queryapi"
                :tableheight="tableheight"
                :fieldsstr="fieldsstr"
                :tableitems="tableitems"
                :btswidth="btswidth"
                :hide-export="hideExport"
                :hide-options="hideOptions"
                :searchtitle="searchtitle"
                :showdateSelector="showdateSelector"
                :hideTool="hideTool"
                :showParkInfo="showParkInfo"
                :hideSearch="hideSearch"
                :hideAdd="hideAdd"
                ref="bolinkuniontable"
        ></common-table>
    </section>
</template>


<script>
    import {path, checkURL, checkUpload, checkNumber, percision} from '../../api/api';
    import util from '../../common/js/util'
    import common from '../../common/js/common'
    import CommonTable from '../../components/CommonTable'

    export default {
        components: {
            CommonTable
        },
        data() {
            return {
                loading: false,
                hideExport: true,
                hideSearch: true,
                showdateSelector: true,
                hideAdd: true,
                tableheight: '',
                showdelete: true,
                hideOptions: true,
                showParkInfo: true,
                hideTool: true,
                queryapi: '/carrenew/query',
                btswidth: '100',
                fieldsstr: 'id__trade_no__card_id__pay_time__amount_receivable__amount_pay__collector__pay_type__car_number__user_id__limit_time__resume',
                tableitems: [
                    {

                        hasSubs: false,
                        subs: [{
                            label: '编号',
                            prop: 'id',
                            width: '123',
                            type: 'str',
                            editable: true,
                            searchable: true,
                            addable: true,
                            unsortable: true,
                            align: 'center'
                        }]
                    }, {

                        hasSubs: false,
                        subs: [{
                            label: '购买流水号',
                            prop: 'trade_no',
                            width: '123',
                            type: 'str',
                            editable: true,
                            searchable: true,
                            addable: true,
                            unsortable: true,
                            align: 'center'
                        }]
                    }, {

                        hasSubs: false,
                        subs: [{
                            label: '月卡编号',
                            prop: 'card_id',
                            width: '123',
                            type: 'str',
                            editable: true,
                            searchable: true,
                            addable: true,
                            unsortable: true,
                            align: 'center'
                        }]
                    }, {

                        hasSubs: false,
                        subs: [{
                            label: '月卡续费时间',
                            prop: 'pay_time',
                            width: '123',
                            type: 'str',
                            editable: true,
                            searchable: true,
                            addable: true,
                            unsortable: true,
                            align: 'center'
                        }]
                    }, {

                        hasSubs: false,
                        subs: [{
                            label: '应收金额',
                            prop: 'amount_receivable',
                            width: '123',
                            type: 'str',
                            editable: true,
                            searchable: true,
                            addable: true,
                            unsortable: true,
                            align: 'center'
                        }]
                    }, {

                        hasSubs: false,
                        subs: [{
                            label: '实收金额',
                            prop: 'amount_pay',
                            width: '123',
                            type: 'str',
                            editable: true,
                            searchable: true,
                            addable: true,
                            unsortable: true,
                            align: 'center'
                        }]
                    }, {

                        hasSubs: false,
                        subs: [{
                            label: '收费员',
                            prop: 'collector',
                            width: '123',
                            type: 'str',
                            editable: true,
                            searchable: true,
                            addable: true,
                            unsortable: true,
                            align: 'center'
                        }]
                    }, {

                        hasSubs: false,
                        subs: [{
                            label: '缴费类型',
                            prop: 'pay_type',
                            width: '123',
                            type: 'str',
                            editable: true,
                            searchable: true,
                            addable: true,
                            unsortable: true,
                            align: 'center'
                        }]
                    }, {

                        hasSubs: false,
                        subs: [{
                            label: '车牌号',
                            prop: 'car_number',
                            width: '123',
                            type: 'str',
                            editable: true,
                            searchable: true,
                            addable: true,
                            unsortable: true,
                            align: 'center'
                        }]
                    }, {

                        hasSubs: false,
                        subs: [{
                            label: '用户编号',
                            prop: 'user_id',
                            width: '123',
                            type: 'str',
                            editable: true,
                            searchable: true,
                            addable: true,
                            unsortable: true,
                            align: 'center'
                        }]
                    }, {
                        hasSubs: false,
                        subs: [{
                            label: '有效期',
                            prop: 'limit_time',
                            width: '123',
                            type: 'str',
                            editable: true,
                            searchable: true,
                            addable: true,
                            unsortable: true,
                            align: 'center'
                        }]
                    }, {
                        hasSubs: false,
                        subs: [{
                            label: '备注',
                            prop: 'resume',
                            width: '123',
                            type: 'str',
                            editable: true,
                            searchable: true,
                            addable: true,
                            unsortable: true,
                            align: 'center'
                        }]
                    },

                ],
                searchtitle: '查询明细',
                //addtitle:'注册友商',
                addFormRules: {
                    name: [
                        {required: true, message: '请输入名称', trigger: 'blur'}
                    ],
                    url: [
                        {required: true, validator: checkURL, trigger: 'blur'}
                    ],
                    file_id: [
                        {required: true, validator: checkUpload, trigger: 'change'}
                    ],
                    weight: [
                        {required: true, validator: checkNumber, trigger: 'blur'}
                    ],
                    description: [
                        {required: true, message: '请输入描述', trigger: 'blur'},
                        {min: 60, max: 80, message: '长度在 60 到 80 个字符', trigger: 'blur'}
                    ],
                },
                editFormRules: {
                    name: [
                        {required: true, message: '请输入名称', trigger: 'blur'}
                    ],
                    url: [
                        {required: true, validator: checkURL, trigger: 'blur'}
                    ],
                    weight: [
                        {required: true, validator: checkNumber, trigger: 'blur'}
                    ],
                    description: [
                        {required: true, message: '请输入描述', trigger: 'blur'},
                        {min: 60, max: 80, message: '长度在 60 到 80 个字符', trigger: 'blur'}
                    ],
                },
            }
        },
        mounted() {
            window.onresize = () => {
                this.tableheight = common.gwh() - 143;
            }
            this.tableheight = common.gwh() - 143;
        },
        activated() {
            console.log('active')
            window.onresize = () => {
                this.tableheight = common.gwh() - 143;
            }
            this.tableheight = common.gwh() - 143;
            //alert("1>>>>>>"+this.searchDate);
            // var dates =this.searchDate;
            // if(dates==undefined ||dates==''){
            //     const end = new Date();
            //     const start = new Date();
            //     start.setTime(start.getTime() - 3600 * 1000 * 24 * 10);
            //     const bday = start.getFullYear()+"-"+(start.getMonth()+1)+"-"+start.getDate();
            //     const eday = end.getFullYear()+"-"+(end.getMonth()+1)+"-"+end.getDate();
            //     dates =bday+" 00:00:00至"+eday+" 23:59:59";
            //     this.searchDate=dates;
            //     //alert("2>>>>>>"+dates)
            // }
            // alert("3>>>>>>"+dates)
            this.$refs['bolinkuniontable'].$refs['search'].resetSearch()
            this.$refs['bolinkuniontable'].getTableData({})
        }
    }

</script>

<style>
    .gutter {
        display: none
    }
</style>

