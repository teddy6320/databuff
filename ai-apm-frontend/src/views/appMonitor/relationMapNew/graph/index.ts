
import { Graph } from 'g6-v5';
import type { GraphData, NodeData, EdgeData, ElementDatum, GraphOptions, } from 'g6-v5';
import { defaultOption } from './config'

export default class TopoGraph {
	private graph: Graph;
  private options: GraphOptions = {
    ...defaultOption
  }

	constructor(options: GraphOptions) {
    this.options = {
      ...defaultOption,
      ...options,
    }
		this.graph = new Graph({
      ...this.options,
		});

    this.render();
	}

  /**
   * 渲染图
   */
  render(): void {
    if (!this.options.container) {
      console.warn('Graph container is not defined.');
      return;
    }
    this.graph?.render();

  }

	/**
	 * 更新图数据
	 */
	updateData(data: GraphData): void {
		this.graph.updateData(data);
	}

	/**
	 * 绑定事件
	 */
	bindEvents(events: Record<string, (...args: any[]) => void>): void {
		Object.entries(events).forEach(([event, handler]) => {
			this.graph?.on(event, handler);
		});
	}

	/**
	 * 查询节点
	 */
	findNode(id: string): ElementDatum | undefined {
		return this.graph?.getElementData(id);
	}

	/**
	 * resize图
	 */
	resize(width: number, height: number): void {
		this.graph?.resize(width, height);
	}

	/**
	 * 获取原始G6实例
	 */
	getInstance(): Graph {
		return this.graph;
	}

  /**
   * 销毁图实例
   */
  destroy(): void {
    this.graph?.destroy();
  }
}
